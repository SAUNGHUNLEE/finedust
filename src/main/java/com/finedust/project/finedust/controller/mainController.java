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
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/main")
public class mainController {
    private final AlarmIssuedRepository alarmIssuedRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MainService mainService;

    public mainController(AlarmIssuedRepository alarmIssuedRepository, SimpMessagingTemplate messagingTemplate, MainService mainService) {
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


    @MessageMapping("/connect") //클->서
 /*   @SendTo("/topic/alarm")//메서드 완료후 메시지를 클라이언트로*/
    public void refreshConnection() {
        System.out.println("/app/connect 실행");
        // 모든 알람 정보 조회
        List<AlarmIssued> alarms = alarmIssuedRepository.findAllOrderByTime();
        if (!alarms.isEmpty()) {
            // DTO 변환
            List<AlarmIssuedDTO> alarmsDTO = alarms.stream().map(alarm -> AlarmIssuedDTO.builder()
                    .measurementName(alarm.getMeasurementName())
                    .message(alarm.getMessage())
                    .time(alarm.getTime())
                    .build()).collect(Collectors.toList());

            System.out.println(alarmsDTO.get(0) + "첫번쨰 값");
            // 클라이언트에게 전체 알람 정보 리스트 전송
            messagingTemplate.convertAndSend("/topic/alarm", alarmsDTO);
        } else {
            // 알람 정보가 없을 경우, 적절한 메시지 전송
            messagingTemplate.convertAndSend("/topic/alarm", Collections.emptyList());
        }
    }

}
