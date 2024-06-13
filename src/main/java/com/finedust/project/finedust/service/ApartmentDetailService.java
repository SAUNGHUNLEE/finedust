package com.finedust.project.finedust.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.finedust.project.finedust.dto.ResponseDTO;
import com.finedust.project.finedust.model.AirQuality;
import com.finedust.project.finedust.persistence.AirQualityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
public class ApartmentDetailService {
    private final AirQualityRepository airQualityRepository;

    @Value("${openApi.decodeServiceKey}")
    private String serviceKey;

    @Value("${kakao.RESTAPI}")
    private String APIKey;

    private final WebClient webClient;

    @Autowired
    public ApartmentDetailService(AirQualityRepository airQualityRepository, WebClient webClient) {
        this.airQualityRepository = airQualityRepository;
        this.webClient = webClient;
    }

    @Value("${google.api}")
    private String APIKeys;


    //OpenApi의 Json데이터 형식
    //response -> body -> items -> 원하는 정보모음
    //해당 데이터들을 비동기적으로 가져오고, DB에 저장(동기적으로)
    public Flux<ResponseDTO.AirQualityData> getOpenApi(JsonNode root) {
        long startTime = System.currentTimeMillis();
        if (root == null || !root.has("response")) {
            throw new IllegalArgumentException("json데이터 없음");
        }
        JsonNode items = root.path("response").path("body").path("items");
        if (!items.isArray()) {
            return Flux.error(new IllegalStateException("item에 대한 정보가 없습니다."));
        }
        // 지역별 데이터 카운트를 위한 맵 초기화
        Map<String, AtomicInteger> regionDataCount = new ConcurrentHashMap<>();

        return Flux.fromIterable(items)
                //.distinct(item -> item.path("stationName").asText() + item.path("dataTime").asText())
                .flatMapSequential(item -> {
                    String stationName = item.path("stationName").asText();
                    String sidoName = item.path("sidoName").asText();
                    String dataTime = item.path("dataTime").asText();
                    String pm10Value = item.path("pm10Value").asText();
                    String pm10Grade = item.path("pm10Grade").asText();
                    String pm25Value = item.path("pm25Value").asText();
                    String pm25Grade = item.path("pm25Grade").asText();

                    ResponseDTO.AirQualityData airQualityData = new ResponseDTO.AirQualityData(pm10Value,pm25Value,dataTime,stationName,sidoName,pm25Grade,pm10Grade);
                    // 지역 데이터 카운트 증가
                    regionDataCount.computeIfAbsent(sidoName, k -> new AtomicInteger(0)).incrementAndGet();

                    return Mono.fromRunnable(() ->
                            {
                                saveOpenApiData(Collections.singletonList(airQualityData));
                            }).thenReturn(airQualityData);
                })
                .doOnComplete(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    // 각 지역별로 로그 남기기
                    regionDataCount.forEach((region, count) -> {
                        log.info("지역: {}, 데이터 갯수: {}, 데이터 불러오는 속도 {} ms", region, count.get(),duration);
                    });
                })
                .onErrorResume(error -> {
                    return Flux.empty();
                });
    }

   public void fetchAndSaveAllAirQualityData() {
        String[] sidoNames = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주", "세종"};
        Flux.fromArray(sidoNames)
                .flatMap(sidoName -> {
                    try {
                        return fetchAirQualityData(sidoName);
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


    //OpenApi접근 (비동기적)
    public Flux<ResponseDTO.AirQualityData> fetchAirQualityData(String sidoName) throws UnsupportedEncodingException{
        String encodeSidoName = URLEncoder.encode(sidoName,"UTF-8");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "json")
                        .queryParam("sidoName", encodeSidoName)
                        .queryParam("numOfRows", 300)
                        .queryParam("ver",1.0) //버전 명시 안할시 pm2.5호출안됨
                        .build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(JsonNode.class)  //OpenApi 에서 가져온 여러개의 json데이터들로 이루어진 문서를 1개의 문서로 보겠다는 뜻.
                .flatMapMany(this::getOpenApi); //1개의 문서에 대한 데이터들을 Flux형태로 봄
    }


/*
**
     *
     * Mono.fromRunnable()를 비동기적으로 사용하기 위해선 별도의 스레드를 할당해주는 .subscribeOn()을 해줘야한다.
     * 없을시, 한스레드에서 작업이 이루어져 동기적으로 동작하는 거랑 동일하다.
     * 
     * GPT 설명
     * 기본적으로, Mono.fromRunnable()은 호출 스레드에서 실행되므로, 스프링 웹 애플리케이션에서 HTTP 요청을 처리하는 스레드에서 바로 실행됩니다.
     * 이 경우, 메소드의 실행이 요청 처리와 동일한 스레드에서 이루어지기 때문에 기본적으로 동기적으로 동작하는 것과 같습니다.

*/




    @Transactional
    public void saveOpenApiData(List<ResponseDTO.AirQualityData> dataList) {
        for(ResponseDTO.AirQualityData dataLists : dataList){
            Optional<AirQuality> existingData = airQualityRepository.findByStationNameAndSidoName(
                    dataLists.getStationName(), dataLists.getSidoName());
            if(!existingData.isPresent()){
                AirQuality newRecord = new AirQuality();
                // 데이터 세팅
                newRecord.setDataTime(dataLists.getDataTime());
                newRecord.setPm10Value(dataLists.getPm10Value());
                newRecord.setPm10Grade(dataLists.getPm10Grade());
                newRecord.setPm25Value(dataLists.getPm25Value());
                newRecord.setPm25Grade(dataLists.getPm25Grade());
                newRecord.setSidoName(dataLists.getSidoName());
                newRecord.setStationName(dataLists.getStationName());

                log.info(newRecord + "새로 저장된 데이터");
                // 데이터베이스에 저장
                airQualityRepository.save(newRecord);
            }else{
                AirQuality existData = existingData.get();
                existData.setDataTime(dataLists.getDataTime());
                existData.setPm10Value(dataLists.getPm10Value());
                existData.setPm10Grade(dataLists.getPm10Grade());
                existData.setPm25Value(dataLists.getPm25Value());
                existData.setPm25Grade(dataLists.getPm25Grade());

                log.info("중복 데이터 발견, data,pm10,pm2.5만 update" + existingData.get());
                airQualityRepository.save(existData);
            }
        }
    }




    //DB접근 메서드
    public Mono<ResponseDTO.AirQualityData> getAirQualityData(String sidoName){

        return Mono.justOrEmpty(airQualityRepository.findByRegionName(sidoName))
                .subscribeOn(Schedulers.boundedElastic()) // 스레드분리
                .map(aq -> ResponseDTO.AirQualityData.builder()
                        .sidoName(aq.getSidoName())
                        .stationName(aq.getStationName())
                        .dataTime(aq.getDataTime())
                        .pm10Value(aq.getPm10Value())
                        .pm10Grade(aq.getPm10Grade())
                        .pm25Value(aq.getPm25Value())
                        .pm25Grade(aq.getPm25Grade())
                        .build())
                .doOnNext(aq -> System.out.println("시 이름 : " + aq.getSidoName() + " " +  "구 이름 : " +  aq.getStationName() + " " + "측정시간 : " + aq.getDataTime() + " " + "미세먼지 수치 : " + aq.getPm10Value() +
                        " "  + aq.getPm10Grade() + " 미세먼지 등급"))
                .doOnError(error -> log.error("데이터 조회 에러: ", error));
    }


    // 위도+경도 로부터 시와 구 정보 추출
    // 카카오 API접근
    public Mono<Map<String, String>> getAddressFromCoords(Double lon, Double lat) throws UnsupportedEncodingException {
        return WebClient.builder().baseUrl("https://dapi.kakao.com")
                .build().get()
                .uri(builder -> builder.path("/v2/local/geo/coord2address.json")
                        .queryParam("x", lon)
                        .queryParam("y", lat)
                        .build())
                .header("Authorization", "KakaoAK " + APIKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(jsonNode -> {
                    if (jsonNode != null && jsonNode.has("documents") && jsonNode.get("documents").size() > 0){
                        JsonNode address = jsonNode.get("documents").get(0).get("address");
                        String city = address.get("region_1depth_name").asText();
                        String region = address.get("region_2depth_name").asText();

                        System.out.println(city + "시");
                        System.out.println(region + "구");

                        return Mono.just(Map.of(
                                "city",city,
                                "region",region
                        ));
                    }else {
                        return Mono.error(new RuntimeException("주소를 찾을수 없습니다."));
                    }
                });


/*  구글 맵 이용
            Mono<String> result = WebClient.builder()
                    .baseUrl("https://maps.googleapis.com")
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("latlng", lat + "," + lon)
                            .queryParam("key", APIKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(System.out::println); // 응답 로깅

            return result.block(); // 동기식 블록으로 결과 반환
*/

    }

}
