package com.finedust.project.finedust.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finedust.project.finedust.dto.ResponseDTO;
import com.finedust.project.finedust.model.AirQuality;
import com.finedust.project.finedust.persistence.AirQualityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class ApartmentDetailService {

    private final String BASE_URL = "https://apis.data.go.kr";
    private final AirQualityRepository airQualityRepository;

    @Value("${kakao.RESTAPI}")
    private String APIKey;

    public ApartmentDetailService(AirQualityRepository airQualityRepository) {
        this.airQualityRepository = airQualityRepository;
    }

/*    @Value("${google.api}")
    private String APIKey;*/



    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request Headers: {}", clientRequest.headers());
            log.info("Request Method: {}", clientRequest.method());
            log.info("Request URI: {}", clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("응답 코드: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }


    @Bean
    public WebClient webClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(BASE_URL);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // 서비스 키를 제외한 값만 인코딩

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(BASE_URL)
                .filter(logRequest())
                .filter(logResponse())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    private String getValidText(JsonNode node, String key) {
        if (node.has(key) && !node.get(key).asText().isEmpty()) {
            return node.get(key).asText();
        }
        return "데이터 없음";
    }


    @Transactional
    public Flux<ResponseDTO.AirQualityData> saveOpenApiData(JsonNode root) {
        if (root == null || !root.has("response")) {
            throw new IllegalArgumentException("json데이터 없음");
        }
        JsonNode items = root.path("response").path("body").path("items");
        if (!items.isArray()) {
            return Flux.error(new IllegalStateException("item에 대한 정보가 없습니다."));
        }

        return Flux.fromIterable(items)
                .flatMap(item -> {
                    // 필드 추출
                    String stationName = getValidText(item, "stationName");
                    String sidoName = getValidText(item, "sidoName");
                    String dataTime = getValidText(item, "dataTime");
                    String pm10Value = getValidText(item, "pm10Value");
                    String pm10Grade = getValidText(item, "pm10Grade");
                    String pm25Value = getValidText(item, "pm25Value");
                    String pm25Grade = getValidText(item, "pm25Grade");

                    // DB 조회
                    return Mono.justOrEmpty(airQualityRepository.findByStationNameAndSidoName(stationName, sidoName))
                            .doOnSuccess(airQuality -> {
                                if (airQuality == null) {
                                    log.info("기존 레코드 없음. 새 AirQuality 객체 생성.");
                                } else {
                                    log.info("기존 AirQuality 레코드 발견. 레코드 업데이트.");
                                }
                            })
                            .defaultIfEmpty(new AirQuality()) // 데이터가 없으면 새 객체 생성
                            .map(airQuality -> {
                                airQuality.setSidoName(sidoName);
                                airQuality.setStationName(stationName);
                                airQuality.setDataTime(dataTime);
                                airQuality.setPm10Value(pm10Value);
                                airQuality.setPm10Grade(pm10Grade);
                                airQuality.setPm25Value(pm25Value);
                                airQuality.setPm25Grade(pm25Grade);
                                return airQualityRepository.save(airQuality);
                            });
                })
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 I/O를 위한 별도의 스레드에서 실행
                .map(savedAirQuality -> ResponseDTO.AirQualityData.builder()
                        .sidoName(savedAirQuality.getSidoName())
                        .stationName(savedAirQuality.getStationName())
                        .dataTime(savedAirQuality.getDataTime())
                        .pm10Value(savedAirQuality.getPm10Value())
                        .pm10Grade(savedAirQuality.getPm10Grade())
                        .pm25Value(savedAirQuality.getPm25Value())
                        .pm25Grade(savedAirQuality.getPm25Grade())
                        .build())
                .doOnNext(System.out::println);
    }


    // 위도+경도 로부터 시와 구 정보 추출
    public Map<String, String> getAddressFromCoords(Double lon, Double lat) {
        Mono<JsonNode> result = WebClient.builder().baseUrl("https://dapi.kakao.com")
                .build().get()
                .uri(builder -> builder.path("/v2/local/geo/coord2address.json")
                        .queryParam("x", lon)
                        .queryParam("y", lat)
                        .build())
                .header("Authorization", "KakaoAK " + APIKey)
                .retrieve()
                .bodyToMono(JsonNode.class);

        JsonNode jsonNode = result.block();
        Map<String, String> addressDetails = new HashMap<>();
        if (jsonNode != null && jsonNode.has("documents") && jsonNode.get("documents").size() > 0) {
            JsonNode address = jsonNode.get("documents").get(0).get("address");
            String city = address.get("region_1depth_name").asText();
            String region = address.get("region_2depth_name").asText();
            addressDetails.put("city", city);
            addressDetails.put("region", region);
            System.out.println(region + "시");
            System.out.println(region + "구");
        }
        return addressDetails;

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

            return result.block(); // 동기식 블록으로 결과 반환*/

    }

}
