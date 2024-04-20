package com.finedust.project.finedust.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.finedust.project.finedust.service.ApartmentDetailService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.yaml.snakeyaml.util.UriEncoder;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

@Slf4j
@Component
public class SchedulerOpenApi {

    private final ApartmentDetailService apartmentDetailService;
    private final WebClient webClient;
    private final String BASE_URL = "https://apis.data.go.kr";
    @Value("${openApi.decodeServiceKey}")
    private String serviceKey;

    public SchedulerOpenApi(ApartmentDetailService apartmentDetailService, WebClient webClient) {
        this.apartmentDetailService = apartmentDetailService;
        this.webClient = webClient;
    }


    /**
     * 
     * 실행하자마자 PostConstruct실행해서 db업데이트 or insert
     * 1시간간격으로 스케쥴러 로 openapi데이터 가져오기 dB에
     */
    public void commonUpdateData() throws UnsupportedEncodingException{
        String[] sidoNames = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기"};

        for (String sidoName : sidoNames) {
            String encodeSidoName = URLEncoder.encode(sidoName, "UTF-8");
            apartmentDetailService.webClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("returnType", "json")
                            .queryParam("sidoName", encodeSidoName)
                            .queryParam("numOfRows", 100)
                            .build())
                    .retrieve()
                    .bodyToFlux(JsonNode.class)
                    .subscribeOn(Schedulers.boundedElastic()) // I/O 작업을 위한 스케줄러 설정
                    .flatMap(apartmentDetailService::saveOpenApiData)
                    .subscribe(result -> System.out.println("Data updated for " + sidoName),
                            error -> System.out.println("Error during data update: " + error.getMessage()));
        }
    }

    
/*    @Scheduled(fixedRate = 3600000)
    public void updateOpenApiData() throws UnsupportedEncodingException {
        commonUpdateData();
    }



    @PostConstruct
    public void UpdateOpenApiData() throws UnsupportedEncodingException {
        commonUpdateData();
    }*/
}
