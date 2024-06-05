/*
package com.finedust.project.finedust.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.finedust.project.finedust.dto.ResponseDTO;

import com.finedust.project.finedust.service.RestTemplateService;
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

    private final RestTemplateService restTemplateService;

    public RestTemplateController(RestTemplateService restTemplateService) {
        this.restTemplateService = restTemplateService;
    }

    @GetMapping("api")
    public ResponseEntity<?> getApi() {
        try {
            List<ResponseDTO.AirQualityData> data = restTemplateService.fetchData();
            return ResponseEntity.ok(data);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            log.error("Error fetching data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching data");
        } catch (Exception e) {
            log.error("General error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }




}
*/
