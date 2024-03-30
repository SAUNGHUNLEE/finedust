package com.finedust.project.finedust.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alarm_issued")
public class AlarmIssued {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd HH")
    @Column(name = "time")
    private LocalDateTime time;  //경고 발령 시간

    @Column(name = "measurement_name")
    private String measurementName;

    @Column(name = "warning_level")
    private int warningLevel;  //경고 단계 1>2>3>4

    @Column(name = "message")
    private String message;
}
