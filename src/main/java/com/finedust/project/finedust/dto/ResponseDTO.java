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

        private String pm10Value;
        private String pm25Value;
        private String dataTime;
        private String stationName;
        private String sidoName;
        private String pm25Grade;
        private String pm10Grade;



    }

}
