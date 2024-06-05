/*
package com.finedust.project.finedust.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finedust.project.finedust.dto.ResponseDTO;
import com.finedust.project.finedust.model.AirQuality;
import com.finedust.project.finedust.persistence.AirQualityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RestTemplateService implements WebMvcConfigurer {

    private RestTemplate restTemplate = new RestTemplate();
    private final AirQualityRepository airQualityRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private final static String BASE_URL = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";

    @Value("${openApi.decodeServiceKey}")
    private String serviceKey;

    public RestTemplateService(AirQualityRepository airQualityRepository) {
        this.airQualityRepository = airQualityRepository;
    }


    public List<ResponseDTO.AirQualityData> fetchData() throws UnsupportedEncodingException, JsonProcessingException {

        String[] sidoNames = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주", "세종"};
        List<ResponseDTO.AirQualityData> allData = new ArrayList<>();

        for(String sido : sidoNames){
            long startTime = System.currentTimeMillis();
            String json = OpenAPiData(sido);
            JsonNode jsonNode = objectMapper.readTree(json); //json -> jsonNode
            List<ResponseDTO.AirQualityData> dataForsido = getOpenApi(jsonNode);
            log.info(dataForsido + "모든 시도 데이터들 모음");
            allData.addAll(dataForsido);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("각 도시 {}, DB에 저장까지 걸린 시간 {} ms", sido,duration);
        }

        return allData;
    }


    public String OpenAPiData(String sidoName) {
        try {
            String encodedServiceKey = URLEncoder.encode(serviceKey, "UTF-8");
            String encodedSidoName = URLEncoder.encode(sidoName, "UTF-8");
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("serviceKey", encodedServiceKey)
                    .queryParam("returnType", "json")
                    .queryParam("sidoName", encodedSidoName)
                    .queryParam("numOfRows", "200")
                    .queryParam("ver", "1.0")
                    .build(true)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // JSON 형식 강제
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            log.info("API 응답: " + response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("API 호출 중 오류 발생: ", e);
            throw new RuntimeException("API 호출 실패", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public List<ResponseDTO.AirQualityData> getOpenApi(JsonNode root) {
        if (root == null || !root.has("response")) {
            throw new IllegalArgumentException("json데이터 없음");
        }
        JsonNode items = root.path("response").path("body").path("items");
        if (!items.isArray()) {
            throw new IllegalArgumentException("비어있습니다.");

        }
        List<ResponseDTO.AirQualityData> dataList = new ArrayList<>();
        for (JsonNode node : items) {
            String stationName = node.path("stationName").asText();
            String sidoName = node.path("sidoName").asText();
            String dataTime = node.path("dataTime").asText();
            String pm10Value = node.path("pm10Value").asText();
            String pm10Grade = node.path("pm10Grade").asText();
            String pm25Value = node.path("pm25Value").asText();
            String pm25Grade = node.path("pm25Grade").asText();

            ResponseDTO.AirQualityData airQualityData = new ResponseDTO.AirQualityData(pm10Value, pm25Value, dataTime, stationName, sidoName, pm25Grade, pm10Grade);
            dataList.add(airQualityData);


        }
        saveOpenApiData(dataList);
        return dataList;


    }

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
                log.info("중복 데이터 발견" + existingData.get());
                airQualityRepository.save(existData);

            }
        }
    }

}
*/
