package com.finedust.project.finedust.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "air_quality", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sido_name", "station_name", "date_time"})
})
public class AirQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "pm10_value")
    private String pm10Value;

    @Column(name = "pm25_value")
    private String pm25Value;

    @Column(name = "data_time")
    private String dataTime;

    @Column(name = "station_name")
    private String stationName;

    @Column(name = "sido_name")
    private String sidoName;

    @Column(name = "pm25_grade")
    private String pm25Grade;

    @Column(name = "pm10_grade")
    private String pm10Grade;


}
