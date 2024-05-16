/*
package com.finedust.project.finedust.config;

import com.fasterxml.jackson.core.JsonParser;
import com.finedust.project.finedust.model.FineDust;
import com.finedust.project.finedust.persistence.FineDustRepository;
import org.apache.tomcat.Jar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;



@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private FineDustRepository fineDustRepository;


    @Override
    public void run(String... args) throws Exception {
        DistrictCounter parser = new DistrictCounter();
        List<FineDust> fineDustList = parser.parseJsonFile("C:\\seoul\\202303Seoul.json");
        fineDustRepository.saveAll(fineDustList);
    }
}

*/
