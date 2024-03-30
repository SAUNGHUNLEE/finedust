package com.finedust.project.finedust.controller;

import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.dto.FineDustDTO;
import com.finedust.project.finedust.service.MainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/main")
public class mainController {

    private final MainService mainService;

    public mainController(MainService mainService) {
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
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e + "오류 내용");
                model.addAttribute("warningError", "경보 오류 발생");
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



}
