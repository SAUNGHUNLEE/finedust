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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class MainService {

    private final FineDustRepository fineDustRepository;
    private final AlarmIssuedRepository alarmIssuedRepository;
    private final CheckDayRepository checkDayRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    private int currentIndex = 0;
    private boolean allAlarmsSent = false;
    public MainService(FineDustRepository fineDustRepository, AlarmIssuedRepository alarmIssuedRepository, CheckDayRepository checkDayRepository, SimpMessagingTemplate messagingTemplate) {
        this.fineDustRepository = fineDustRepository;
        this.alarmIssuedRepository = alarmIssuedRepository;
        this.checkDayRepository = checkDayRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 구 + 날짜 선택 시 상세정보
    public FineDustDTO getFineDust(FineDustDTO fineDustDTO) {
        try {
            FineDust fineDust = fineDustRepository.findFineDust(fineDustDTO.getMeasurementName(), fineDustDTO.getDate())
                    .orElseThrow(() -> new IllegalArgumentException("조건에 맞는 정보가 없습니다."));

            FineDustDTO fineDustDTOInfo = FineDustDTO.builder()
                    .id(fineDust.getId())
                    .date(fineDust.getDate())
                    .measurementName(fineDust.getMeasurementName())
                    .measurementCode(fineDust.getMeasurementCode())
                    .pm10(fineDust.getPm10())
                    .pm2_5(fineDust.getPm2_5())
                    .build();

            return fineDustDTOInfo;


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FineDustService.getFineDust() : 에러 발생.");
        }
    }

    /***
     pm10 150이상 이며 2시간 이상 지속시 주의보 발령 , 300이상 경보
     pm2.5 75이상 이며 2시간 이상 지속시 주의보 발령 , 150이상 경보
     미세먼지 정보를 가지고, 요구사항에 해당하면 AlarmIssued테이블에 저장되는 메서드
     날짜,시간,구 선택시 해당 정보가 기준. -2시간 해서 150이상인지 확인(연속적이어야함)

     미세먼지(pm10) 2,4 , 초미세먼지(pm2.5) 1,3  조건 두개 다 충족되면 1에 가까운 숫자 선택(심각도)
     ***/
    public AlarmIssuedDTO getPmWarning(FineDustDTO fineDustDTO) {
        LocalDateTime currentDateTime = fineDustDTO.getDate();//선택한시간
        LocalDateTime beforeDateTime = currentDateTime.minusHours(2);
        List<FineDust> fineDustList = fineDustRepository.findFineDustByPmChoice(fineDustDTO.getMeasurementName(), beforeDateTime, currentDateTime);

        fineDustList.sort(Comparator.comparing(FineDust::getDate));

        boolean isContinuous = false;
        int continuousCount = 0;
        LocalDateTime lastDateTime = null;

        for (FineDust fineDust : fineDustList) {
            // 연속된 시간 체크 로직
            if (lastDateTime == null || fineDust.getDate().minusHours(1).equals(lastDateTime)) {
                continuousCount++;
                if (continuousCount >= 2) {
                    isContinuous = true;
                    break;
                }
            } else {
                continuousCount = 1; // 연속이 끊기면 카운트 리셋
            }
            lastDateTime = fineDust.getDate();
        }

        int pm10Level = 0;
        int pm2_5Level = 0;

        if (isContinuous) {
            for (FineDust fineDust : fineDustList) {
                if (fineDust.getPm10() >= 300) {
                    pm10Level = Math.max(pm10Level, 2); // 경보
                } else if (fineDust.getPm10() >= 150) {
                    pm10Level = Math.max(pm10Level, 4); // 주의보
                }

                if (fineDust.getPm2_5() >= 150) {
                    pm2_5Level = Math.max(pm2_5Level, 1); // 경보
                } else if (fineDust.getPm2_5() >= 75) {
                    pm2_5Level = Math.max(pm2_5Level, 3); // 주의보
                }
            }
        }

        // 둘다 조건 충족 시 level 1에 가까우면 채택
        int finalWarningLevel = Math.min(pm10Level, pm2_5Level);

        if (finalWarningLevel > 0) {
            Optional<AlarmIssued> existingAlarm = alarmIssuedRepository.findByMeasurementNameAndTime(fineDustDTO.getMeasurementName(), currentDateTime);

            if (!existingAlarm.isPresent()) {
                AlarmIssued alarmIssued = AlarmIssued.builder()
                        .measurementName(fineDustDTO.getMeasurementName())
                        .warningLevel(finalWarningLevel)
                        .time(currentDateTime)
                        .message(finalWarningLevel + "단계 경보발령")
                        .build();
                alarmIssuedRepository.save(alarmIssued);

                AlarmIssuedDTO alarmIssuedDTO = AlarmIssuedDTO.builder()
                        .measurementName(alarmIssued.getMeasurementName())
                        .warningLevel(alarmIssued.getWarningLevel())
                        .time(alarmIssued.getTime())
                        .message(finalWarningLevel + "단계 경보발령")
                        .build();
                return alarmIssuedDTO;

            } else {
                return AlarmIssuedDTO.builder()
                        .measurementName(fineDustDTO.getMeasurementName())
                        .warningLevel(0)
                        .time(currentDateTime)
                        .message(finalWarningLevel + "단계 경보발령")
                        .build();
            }
        } else {
            return AlarmIssuedDTO.builder()
                    .measurementName(fineDustDTO.getMeasurementName())
                    .warningLevel(0)
                    .time(currentDateTime)
                    .message("경보발령 기준에 충족하지 않는 날입니다. pm10이 150이상 혹은 300이상, pm2.5이 75이상 혹은 150이상이 2시간 이상 지속되어야합니다.")
                    .build();
        }
    }

    /***
     * FindDust테이블에서 pm10,pm2.5가 0인 결과를 repository에서 다가져온다
     * 경보발령 메서드처럼, 상세정보 페이지에 들어갈때, 조건에 해당하면 checkDay테이블에 값 저장
     */
    public CheckDayDTO getCheckDay(FineDustDTO fineDustDTO) {
        Optional<FineDust> fineDust = fineDustRepository.findCheckDay(fineDustDTO.getMeasurementName(), fineDustDTO.getDate());

        if (fineDust.isPresent()) {
            Optional<CheckDay> checkDay = checkDayRepository.findByMeasurementNameAndCheckTime(fineDust.get().getMeasurementName(), fineDust.get().getDate());

            if (!checkDay.isPresent()) {
                CheckDay checkDays = CheckDay.builder()
                        .measurementName(fineDust.get().getMeasurementName())
                        .measurementCode(fineDust.get().getMeasurementCode())
                        .checkTime(fineDust.get().getDate())
                        .message("정기 점검이 있던 날입니다.")
                        .build();

                checkDayRepository.save(checkDays);

                CheckDayDTO checkDayDTO = CheckDayDTO.builder()
                        .measurementName(checkDays.getMeasurementName())
                        .measurementCode(checkDays.getMeasurementCode())
                        .checkTime(checkDays.getCheckTime())
                        .message(checkDays.getMessage())
                        .build();

                return checkDayDTO;
            } else {
                return CheckDayDTO.builder()
                        .measurementName(checkDay.get().getMeasurementName())
                        .measurementCode(checkDay.get().getMeasurementCode())
                        .checkTime(checkDay.get().getCheckTime())
                        .message(checkDay.get().getMessage())
                        .build();
            }
        } else {
            return CheckDayDTO.builder()
                    .measurementName(fineDustDTO.getMeasurementName())
                    .measurementCode(fineDustDTO.getMeasurementCode())
                    .checkTime(fineDustDTO.getDate())
                    .message("정기점검 날이 아닙니다.")
                    .build();
        }
    }


    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void getAlarmView() {
        List<AlarmIssued> alarmIssuedInfo = alarmIssuedRepository.findAllOrderByTime();
        if (alarmIssuedInfo.isEmpty()) {
            messagingTemplate.convertAndSend("/topic", "3월달 경보 발령 정보가 없습니다.");
            return;
        }
        if(allAlarmsSent){
            return;
        }
        System.out.println(alarmIssuedInfo.size() + "알림 사이즈");
        if(currentIndex < alarmIssuedInfo.size()){
            AlarmIssued alarmIssued = alarmIssuedInfo.get(currentIndex++);
            AlarmIssuedDTO alarmIssuedDTO = AlarmIssuedDTO.builder()
                    .measurementName(alarmIssued.getMeasurementName())
                    .message(alarmIssued.getMessage())
                    .time(alarmIssued.getTime())
                    .build();
            messagingTemplate.convertAndSend("/topic/alarm", alarmIssuedDTO); //서버->클라이언트
            System.out.println(alarmIssuedDTO + "정보");
        } else {
            allAlarmsSent = true;
        }
    }




}
