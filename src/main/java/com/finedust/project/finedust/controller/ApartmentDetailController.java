package com.finedust.project.finedust.controller;

import com.finedust.project.finedust.config.SchedulerOpenApi;
import com.finedust.project.finedust.dto.ResponseDTO;
import com.finedust.project.finedust.persistence.AirQualityRepository;
import com.finedust.project.finedust.service.ApartmentDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/detail")
public class ApartmentDetailController {
    private final ApartmentDetailService apartmentDetailService;
    private final AirQualityRepository airQualityRepository;

    private final SchedulerOpenApi schedulerOpenApi;

    @Autowired
    public ApartmentDetailController(ApartmentDetailService apartmentDetailService, AirQualityRepository airQualityRepository, SchedulerOpenApi schedulerOpenApi) {
        this.apartmentDetailService = apartmentDetailService;
        this.airQualityRepository = airQualityRepository;
        this.schedulerOpenApi = schedulerOpenApi;
    }



    @ResponseBody
    @GetMapping(value = "/getAll", produces = "application/json;charset=utf-8")
    public ResponseEntity<String> getRegionInfo() throws UnsupportedEncodingException {
        schedulerOpenApi.commonUpdateData();
        return ResponseEntity.ok("데이터 요청 및 저장이 완료되었습니다.");
    }



    @PostMapping(value = "/getRegionData", produces = "application/json;charset=utf-8")
    public Mono<ResponseDTO.AirQualityData> getRegionData(@RequestBody Map<String,Double> coords) throws UnsupportedEncodingException {
        // 좌표 추출
        Double longitude = coords.get("longitude");
        Double latitude = coords.get("latitude");


        return apartmentDetailService.getAddressFromCoords(longitude, latitude)
                .flatMap(addressDetails ->{
                    String regionName = addressDetails.get("region");
                    return apartmentDetailService.getAirQualityData(regionName);
                });

    }


    //요청 보내기
/*
   @ResponseBody
    @PostMapping(value = "get", produces = "application/json;charset=utf-8")
    public String getData(@RequestParam("sidoName") String sidoName) throws Exception {
        //요청 url 전달
        // url = 요청주소 + ?serviceKey="+serviceKey
        String url = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?serviceKey="+serviceKey;
        url += "&returnType=json"; //json으로 파라미터 보내기
        url += "&sidoName="+ URLEncoder.encode(sidoName,"UTF-8"); //필수 요청변수(서비스키, 시도명), 요청시 전달값 중 한글이 있을 경우 encoding 해야함
        url += "&numOfRows="+10000;	//한 페이지 결과 수(옵션 변수)

        //단순한 문자열로 정의한 url을 자바에서 활용할 수 있는 객체로 변환
        URL requestURL = new URL(url);
        //목적지로 향하는 다리 건설
        HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();

        conn.setRequestMethod("GET");	//GET방식으로 요청

        //OpenAPI 서버로 요청 후 입력 스트림을 통해 응답데이터 읽어들이기
        //conn 다리가 건설되어 있는 목적지로부터 데이터를 읽어와야 함
        //1. conn 목적지로부터 inputStream 생성(conn.getInputStream())
        InputStream is = conn.getInputStream();
        //2. 생성된 InputStream을 이용하기 위한 객체 생성(new InputStreamReader())
        InputStreamReader isr = new InputStreamReader(is);

        //3. InputStreamReader 객체보다 편한 BufferedReader 사용을 위해 객체 생성
        BufferedReader br = new BufferedReader(isr) ;

        //생성된 BufferedReader를 이용해서 데이터를 읽고 활용하기
        String result = "";
        String line = "";
        while(true) {
            line = br.readLine();
            if(line == null) {break;}
            result += line;

        }
        System.out.println(result);

        //사용한 스트림 반납
        br.close();
        conn.disconnect();
        return result; //String으로 했기 때문에 리턴 null 달아줌
    }
*/





}
