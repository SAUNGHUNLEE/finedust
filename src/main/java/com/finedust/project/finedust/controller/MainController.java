package com.finedust.project.finedust.controller;

import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.dto.CheckDayDTO;
import com.finedust.project.finedust.dto.FineDustDTO;
import com.finedust.project.finedust.model.AlarmIssued;
import com.finedust.project.finedust.persistence.AlarmIssuedRepository;
import com.finedust.project.finedust.service.MainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/main")
public class MainController {
    private int lastSentIndex = 0; // 최근에 전송한 알람의 인덱스를 저장하는 변수

    private final AlarmIssuedRepository alarmIssuedRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MainService mainService;

    public MainController(AlarmIssuedRepository alarmIssuedRepository, SimpMessagingTemplate messagingTemplate, MainService mainService) {
        this.alarmIssuedRepository = alarmIssuedRepository;
        this.messagingTemplate = messagingTemplate;
        this.mainService = mainService;
    }

    @GetMapping("/seoul")
    public String getMain() {
        return "main";
    }

    @GetMapping("/info")
    public String getMainInfo(HttpSession session, Model model) {
        FineDustDTO fineDustDTOInfo = (FineDustDTO) session.getAttribute("fineDustInfo");
     ;
        if (fineDustDTOInfo != null) {
            model.addAttribute("fineDustInfo", fineDustDTOInfo);
            try{
                AlarmIssuedDTO alarmIssuedDTO = mainService.getPmWarning(fineDustDTOInfo);
                model.addAttribute("warningResponse", alarmIssuedDTO);

                CheckDayDTO checkDayDTO = mainService.getCheckDay(fineDustDTOInfo);
                model.addAttribute("checkResponse", checkDayDTO);

            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e + "오류 내용");
                model.addAttribute("warningError", "경보 오류 발생");
                model.addAttribute("checkError", "점검 정보 오류 발생");
            }
            System.out.println(fineDustDTOInfo + "세션에서 담긴 정보 반환");
/*            session.removeAttribute("fineDustInfo"); // 사용 후 세션에서 삭제*/
        }
        return "mainInfo";
    }

    // 미세먼지 정보
    @PostMapping("/info")
    public String viewProfile(@RequestParam("measurementName") String measurementName,
                              @RequestParam("date") LocalDateTime date,
                              Model model,
                              HttpServletRequest request) {
        try {
            FineDustDTO fineDustDTO = FineDustDTO.builder()
                    .measurementName(measurementName)
                    .date(date)
                    .build();

            FineDustDTO fineDustDTOInfo = mainService.getFineDust(fineDustDTO);
            request.getSession().setAttribute("fineDustInfo", fineDustDTOInfo);
            System.out.println(fineDustDTOInfo + "선택한 정보 저장");
            System.out.println(request + "세션 정보 저장");
            return "redirect:/main/info"; // GET 요청으로 리디렉트
        } catch (Exception e) {
            model.addAttribute("error", "조회 중 오류가 발생했습니다.");
            return "errorPage";
        }
    }


    // 클라이언트에서 초기 데이터 요청을 처리하는 메소드

    @MessageMapping("/loadInitialAlarms")
    public void loadInitialAlarms() {
        System.out.println("loadInitialAlars 통과");
        List<AlarmIssued> alarms = alarmIssuedRepository.findAllOrderByTime();
        List<AlarmIssuedDTO> alarmsDTO = alarms.stream()
                .map(alarm -> AlarmIssuedDTO.builder()
                        .id(alarm.getId())
                        .measurementName(alarm.getMeasurementName())
                        .message(alarm.getMessage())
                        .time(alarm.getTime())
                        .build())
                .collect(Collectors.toList());

        System.out.println(alarmsDTO + "controller");
        messagingTemplate.convertAndSend("/topic/alarm", alarmsDTO);
    }


  /*
  @MessageMapping("/connect") // 클->서
    public void refreshConnection(@Payload Map<String, Object> payload) {
        int clientLastSentIndex = Integer.parseInt(payload.get("lastSentIndex").toString());
        lastSentIndex = Math.max(lastSentIndex, clientLastSentIndex);
        System.out.println("/app/connect 첫번째연결");
        // 이미 전송한 알람 중 lastSentIndex 이후의 알람만 조회
        List<AlarmIssued> newAlarms = alarmIssuedRepository.findAlarmsAfter(lastSentIndex);

        if (!newAlarms.isEmpty()) {
            List<AlarmIssuedDTO> alarmsDTO = newAlarms.stream()
                    .map(alarm -> AlarmIssuedDTO.builder()
                            .measurementName(alarm.getMeasurementName())
                            .message(alarm.getMessage())
                            .time(alarm.getTime())
                            .build())
                    .collect(Collectors.toList());

            messagingTemplate.convertAndSend("/topic/alarm", alarmsDTO);

            // 최신으로 전송한 알람의 인덱스 업데이트
            lastSentIndex = newAlarms.get(newAlarms.size() - 1).getId();
        }
    }
    */




}
