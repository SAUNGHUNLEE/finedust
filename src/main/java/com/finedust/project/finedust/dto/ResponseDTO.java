package com.finedust.project.finedust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ResponseDTO {

    private Body body;
    @Data
    @AllArgsConstructor
    public static class Body{
        private int totalCount;
        private List<AirQualityData> items;

    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class AirQualityData{

        private String so2Grade;
        private String coFlag;
        private String khaiValue;
        private String so2Value;
        private String coValue;
        private String pm25Flag;
        private String pm10Flag;
        private String o3Grade;
        private String pm10Value;
        private String khaiGrade;
        private String pm25Value;
        private String sidoName;
        private String no2Flag;
        private String no2Grade;
        private String o3Flag;
        private String pm25Grade;
        private String so2Flag;
        private String dataTime;
        private String coGrade;
        private String no2Value;
        private String stationName;
        private String pm10Grade;
        private String o3Value;
    }

}
