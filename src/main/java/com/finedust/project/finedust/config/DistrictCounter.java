package com.finedust.project.finedust.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finedust.project.finedust.model.FineDust;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DistrictCounter {
    
    //json -> java 객체 변환 리스트 -> dataLoader에서 받아서 db저장
 /*  public List<FineDust> parseJsonFile(String filePath){
        ObjectMapper objectMapper = new ObjectMapper();
        List<FineDust> fineDustList = null;
        try{
            fineDustList = objectMapper.readValue(new File(filePath), new TypeReference<List<FineDust>>() {});
        }catch(Exception e){
            e.printStackTrace();;
        }
        return fineDustList;
    }*/
   /*   25개구 출력
   public static void main(String[] args) {
        try {

            // JSON 파일 로드
            File jsonFile = new File("C:\\seoul\\202303Seoul.json"); // JSON 파일 경로 지정
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            // 각 구별 카운트를 저장할 HashMap 생성
            Map<String, Integer> districtCount = new HashMap<>();

            // JSON 배열 순회
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    String districtName = node.get("측정소명").asText();
                    // HashMap에 카운트 추가
                    districtCount.put(districtName, districtCount.getOrDefault(districtName, 0) + 1);
                }
            }


            for (Map.Entry<String, Integer> entry : districtCount.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }*/
}