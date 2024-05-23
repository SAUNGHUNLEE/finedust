package com.finedust.project.finedust.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finedust.project.finedust.service.ApartmentDetailService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
public class SchedulerOpenApi {


    //private final RestTemplateService restTemplateService;
    private final ApartmentDetailService apartmentDetailService;

    public SchedulerOpenApi(ApartmentDetailService apartmentDetailService) {
        this.apartmentDetailService = apartmentDetailService;
    }



/*

      실행하자마자 PostConstruct실행해서 db업데이트 or insert
      1시간간격으로 스케쥴러 로 openapi데이터 가져오기 db에
*/


    public void commonUpdateData() throws UnsupportedEncodingException {
            String[] sidoNames = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주", "세종"};
        //String[] sidoNames = {"전국"};
        Flux.fromArray(sidoNames)
                    .flatMap(sidoName -> {
                        try {
                            log.info(sidoName + "시 도 이름");
                            return apartmentDetailService.fetchAirQualityData(sidoName);
                        } catch (UnsupportedEncodingException e) {
                            return Flux.error(new RuntimeException("인코딩 실패: " + e.getMessage(), e));
                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            data -> System.out.println(data + " DB에 저장 완료"),
                            error -> System.err.println("데이터 저장 에러 발생: " + error.getMessage())
                    );
    }



    @Scheduled(fixedRate = 3600000)
    public void updateOpenApiData() throws UnsupportedEncodingException {
        commonUpdateData();
    }



   @PostConstruct
    public void UpdateOpenApiData() throws UnsupportedEncodingException {
        commonUpdateData();
    }

/*    @PostConstruct
    public void RestTemplateUpdateOpenApiData() throws UnsupportedEncodingException, JsonProcessingException {
        restTemplateService.fetchData();
    }*/
}
