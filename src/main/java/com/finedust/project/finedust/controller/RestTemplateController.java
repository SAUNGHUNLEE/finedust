package com.finedust.project.finedust.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finedust.project.finedust.dto.ResponseDTO;

import com.finedust.project.finedust.service.RestTemplateService;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/detail")
public class RestTemplateController {

    private final Bucket bucket;
    private final RestTemplateService restTemplateService;

    public RestTemplateController(Bucket bucket,RestTemplateService restTemplateService) {
        this.bucket = bucket;
        this.restTemplateService = restTemplateService;
    }

    @GetMapping("api")
    public ResponseEntity<?> getApi() {
        try {
            boolean consume = bucket.tryConsume(1);
            if(consume){
                List<ResponseDTO.AirQualityData> data = restTemplateService.fetchData();
                System.out.println("bucket4j에 맞게 호출중");
                return ResponseEntity.ok(data);
            }else{
                System.out.println("api 호출 과함");
                throw new IllegalStateException("과도한 API 호출");
            }

        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            log.error("Error fetching data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching data");
        } catch (Exception e) {
            log.error("General error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }




}
