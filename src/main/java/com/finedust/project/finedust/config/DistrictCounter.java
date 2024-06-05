/*
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
    
    //dataLoader에서 받아서 db저장
 public List<FineDust> parseJsonFile(String filePath){
        ObjectMapper objectMapper = new ObjectMapper();
        List<FineDust> fineDustList = null;
        try{
            fineDustList = objectMapper.readValue(new File(filePath), new TypeReference<List<FineDust>>() {});
        }catch(Exception e){
            e.printStackTrace();;
        }
        return fineDustList;
    }

}*/
