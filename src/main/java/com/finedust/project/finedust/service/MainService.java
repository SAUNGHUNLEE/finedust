package com.finedust.project.finedust.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.dto.CheckDayDTO;
import com.finedust.project.finedust.dto.FineDustDTO;
import com.finedust.project.finedust.model.AlarmIssued;
import com.finedust.project.finedust.model.CheckDay;
import com.finedust.project.finedust.model.FineDust;
import com.finedust.project.finedust.persistence.AlarmIssuedRepository;
import com.finedust.project.finedust.persistence.CheckDayRepository;
import com.finedust.project.finedust.persistence.FineDustRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class MainService {

    private final FineDustRepository fineDustRepository;
    private final AlarmIssuedRepository alarmIssuedRepository;
    private final CheckDayRepository checkDayRepository;

    public MainService(FineDustRepository fineDustRepository, AlarmIssuedRepository alarmIssuedRepository, CheckDayRepository checkDayRepository) {
        this.fineDustRepository = fineDustRepository;
        this.alarmIssuedRepository = alarmIssuedRepository;
        this.checkDayRepository = checkDayRepository;
    }

    private AlarmIssuedDTO convertToDTO(AlarmIssued alarmIssued) {
        return AlarmIssuedDTO.builder()
                .measurementName(alarmIssued.getMeasurementName())
                .warningLevel(alarmIssued.getWarningLevel())
                .time(alarmIssued.getTime())
                .message(alarmIssued.getMessage() + "단계 경보 발령")
                .build();
    }
    /***
     pm10 150이상 이며 2시간 이상 지속시 주의보 발령 , 300이상 경보
     pm2.5 75이상 이며 2시간 이상 지속시 주의보 발령 , 150이상 경보
     미세먼지 정보를 가지고, 요구사항에 해당하면 AlarmIssued테이블에 저장되는 메서드
     날짜,시간,구 선택시 해당 정보가 기준. -2시간 해서 150이상인지 확인(연속적이어야함)

     미세먼지(pm10) 2,4 , 초미세먼지(pm2.5) 1,3  조건 두개 다 충족되면 1에 가까운 숫자 선택(심각도)
     ***/
    public List<AlarmIssuedDTO> getPmWarning() {

        List<FineDust> fineDustInfo = fineDustRepository.findAllDust();
        List<AlarmIssuedDTO> alarmIssueds = new ArrayList<>();

        boolean isContinuous = false;
        int continuousCount = 0;
        LocalDateTime lastDateTime = null;
        LocalDateTime fineDustDTODateInfo;

        for (int i = 0; i < fineDustInfo.size(); i++) {
            fineDustDTODateInfo = fineDustInfo.get(i).getDate();

            // 연속된 시간 체크 로직
            if (lastDateTime == null || (fineDustDTODateInfo.minusHours(1).equals(lastDateTime))) {
                continuousCount++;
                System.out.println(continuousCount + "연속된 시간");
                if (continuousCount >= 3) {
                    isContinuous = true;
                    System.out.println(isContinuous + "갯수");
                    break;
                }
            } else {
                continuousCount = 1; // 연속이 끊기거면 리셋
            }
            lastDateTime = fineDustDTODateInfo;
        }

        int finalWarningLevel = Integer.MAX_VALUE;

        if (isContinuous) {
            System.out.println(isContinuous + "연속된 시간");
            for (FineDust fineDusts : fineDustInfo) {
                int pm10Level = Integer.MAX_VALUE;
                int pm2_5Level = Integer.MAX_VALUE;

                // PM10 조건에 따른 레벨 결정
                if (fineDusts.getPm10() >= 300) {
                    pm10Level = 2; // 미세먼지 경보
                } else if (fineDusts.getPm10() >= 150) {
                    pm10Level = 4; // 미세먼지 주의보
                }

                // PM2.5 조건에 따른 레벨 결정
                if (fineDusts.getPm2_5() >= 150) {
                    pm2_5Level = 1; // 초미세먼지 경보
                } else if (fineDusts.getPm2_5() >= 75) {
                    pm2_5Level = 3; // 초미세먼지 주의보
                }

                finalWarningLevel = Math.min(pm10Level, pm2_5Level);
                if (finalWarningLevel != Integer.MAX_VALUE) {
                    Optional<AlarmIssued> existingAlarmIssued = alarmIssuedRepository.findByMeasurementName(fineDusts.getMeasurementName(), fineDusts.getDate());
                    if (!existingAlarmIssued.isPresent()) {
                        AlarmIssued alarmIssued = AlarmIssued.builder()
                                .measurementName(fineDusts.getMeasurementName())
                                .warningLevel(finalWarningLevel)
                                .time(fineDusts.getDate())
                                .message(finalWarningLevel + "단계 경보발령")
                                .build();
                        alarmIssuedRepository.save(alarmIssued);
                        convertToDTO(alarmIssued);

                        alarmIssueds.add(convertToDTO(alarmIssued));


                    } else {
                        // 기존 경보 정보를 클라이언트에 전송
                        alarmIssueds.add(convertToDTO(existingAlarmIssued.get()));
                        System.out.println("동일한 경보 정보가 이미 존재하여 저장하지 않습니다.");
                    }

                } else {
                    System.out.println("경보발령 기준에 충족하지 않는 데이터입니다.");
                }
            }
        }
        return alarmIssueds; // 모든 데이터 처리 후 반환

    }

    /***
     * FindDust테이블에서 pm10,pm2.5가 0인 결과를 repository에서 다가져온다
     * 경보발령 메서드처럼, 상세정보 페이지에 들어갈때, 조건에 해당하면 checkDay테이블에 값 저장
     */
    public List<CheckDayDTO> getCheckDay( ) {
        List<FineDust> fineDustInfo = fineDustRepository.findCheckDust();
        List<CheckDayDTO> checkDayDTOS = new ArrayList<>();

        for(FineDust fineDusts : fineDustInfo){
            Optional<CheckDay> checkDayInfo = checkDayRepository.findByMeasurementNameAndCheckTime(fineDusts.getMeasurementName(),fineDusts.getDate());
            if(fineDusts.getPm2_5() == 0 && fineDusts.getPm10() == 0){
                if(!checkDayInfo.isPresent()){
                    CheckDay checkDay = CheckDay.builder()
                            .measurementName(fineDusts.getMeasurementName())
                            .measurementCode(fineDusts.getMeasurementCode())
                            .checkTime(fineDusts.getDate())
                            .message("정기점검 있는 날입니다.")
                            .build();

                    checkDayRepository.save(checkDay);

                    CheckDayDTO checkDayDTO = CheckDayDTO.builder()
                            .measurementName(checkDay.getMeasurementName())
                            .measurementCode(checkDay.getMeasurementCode())
                            .checkTime(checkDay.getCheckTime())
                            .message("정기점검 있는 날입니다.")
                            .build();

                    checkDayDTOS.add(checkDayDTO);
                }else{
                    System.out.println("checkDay테이블에 해당 정보가 저장되어있습니다.");
                }
            }
        }
        return checkDayDTOS;
    }

    public FineDustDTO getFineDustInfo(FineDustDTO.SearchDTO searchDTO){
        // DTO에서 필요한 값 추출
        String measurementName = searchDTO.getMeasurementName();
        LocalDateTime date = searchDTO.getDate();

        FineDust fineDust = fineDustRepository.findFineDust(measurementName,date)
                .orElseThrow(() -> new IllegalArgumentException("해당 미세먼지 정보가 없습니다."));
        Optional<AlarmIssued> alarmIssued = alarmIssuedRepository.findByMeasurementName(measurementName,date);
        String message = "";

        if (alarmIssued.isPresent()) {
            AlarmIssued alarmIssuedInfo = alarmIssued.get();
            message = "경보 레벨 " + alarmIssuedInfo.getWarningLevel() + " 입니다";
        } else if (fineDust.getPm2_5() == 0 && fineDust.getPm10() == 0) {
            message = "정기 점검 날입니다.";
        }
        FineDustDTO fineDustDTO = FineDustDTO.builder()
                .measurementName(fineDust.getMeasurementName())
                .measurementCode(fineDust.getMeasurementCode())
                .pm10(fineDust.getPm10())
                .pm2_5(fineDust.getPm2_5())
                .date(fineDust.getDate())
                .message(message)
                .build();

        return fineDustDTO;
    }


}
