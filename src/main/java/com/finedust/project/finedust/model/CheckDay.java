package com.finedust.project.finedust.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_day")
public class CheckDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd HH")
    @Column(name = "check_time")
    private LocalDateTime checkTime;  //점검날짜

    @Column(name = "measurement_name")
    private String measurementName;

    @Column(name = "measurementCode")
    private String measurementCode;

    @Column(name = "message")
    private String message;
}
