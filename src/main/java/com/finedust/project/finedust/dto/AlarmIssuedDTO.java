package com.finedust.project.finedust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmIssuedDTO {
    private int id;
    private String measurementName;
    private int warningLevel;
    private LocalDateTime time;
    private String message;
}
