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
public class CheckDayDTO {
    private int id;
    private String measurementName;
    private String measurementCode;
    private LocalDateTime checkTime;
    private String message;

}
