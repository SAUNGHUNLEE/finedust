package com.finedust.project.finedust.controller;

import com.finedust.project.finedust.config.AlarmClient;
import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.dto.CheckDayDTO;
import com.finedust.project.finedust.dto.FineDustDTO;
import com.finedust.project.finedust.persistence.AlarmIssuedRepository;
import com.finedust.project.finedust.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/main")
public class MainController {
    private int lastSentIndex = 0; // 최근에 전송한 알람의 인덱스를 저장하는 변수

    private final AlarmIssuedRepository alarmIssuedRepository;
    private final MainService mainService;

    private final AlarmClient alarmClient;

    public MainController(AlarmIssuedRepository alarmIssuedRepository, MainService mainService, AlarmClient alarmClient) {
        this.alarmIssuedRepository = alarmIssuedRepository;
        this.mainService = mainService;
        this.alarmClient = alarmClient;
    }

    @GetMapping("/seoul")
    public String getMain(FineDustDTO fineDustDTO, Model model) {
        if (fineDustDTO != null) {
            try{
                List<AlarmIssuedDTO> alarmIssuedDTOS = alarmClient.connectToServer();
                model.addAttribute("alarmInfo",alarmIssuedDTOS);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e + "오류 내용");
            }
        }
        return "main";
    }

    @GetMapping("/info")
    public String getFineDustInfo(@RequestParam("measurementName") String measurementName,
                                  @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd HH") LocalDateTime date,
                                  Model model) {
        try {
            FineDustDTO.SearchDTO searchDTO = FineDustDTO.SearchDTO.builder()
                    .measurementName(measurementName)
                    .date(date)
                    .build();

            FineDustDTO fineDustDTOInfo = mainService.getFineDustInfo(searchDTO);
            model.addAttribute("fineDustDTO", fineDustDTOInfo);
            System.out.println(fineDustDTOInfo + "검색 결과 정보");
            return "mainInfo"; // Thymeleaf 템플릿 이름
        } catch (Exception e) {
            model.addAttribute("error", "데이터 조회 중 오류가 발생했습니다.");
            return "errorPage"; // 오류 표시용 Thymeleaf 템플릿
        }
    }




    // 미세먼지 정보
    @PostMapping("/info")
    public String viewProfile(@RequestParam("measurementName") String measurementName,
                              @RequestParam("date") LocalDateTime date,
                              @ModelAttribute FineDustDTO fineDustDTO) {
        try {
            FineDustDTO.SearchDTO searchDTO = FineDustDTO.SearchDTO.builder()
                    .measurementName(measurementName)
                    .date(date)
                    .build();

            FineDustDTO fineDustDTOInfo = mainService.getFineDustInfo(searchDTO);
            System.out.println(fineDustDTOInfo + "선택한 정보 저장");
            return "redirect:/main/info";
        } catch (Exception e) {
            return "errorPage";
        }
    }


}
