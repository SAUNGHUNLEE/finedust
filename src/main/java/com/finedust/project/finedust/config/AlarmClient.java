package com.finedust.project.finedust.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.service.MainService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



@Component
public class AlarmClient {
/*    public void connectToServer() {
        String serverAddress = "localhost"; // 서버 주소
        int serverPort = 9221; // 서버 포트
        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("서버에 연결됨.");

            String json = reader.readLine(); // 서버로부터 JSON 메시지 수신
            List<AlarmIssuedDTO> alarmList = JsonUtil.fromJson(json, new TypeToken<List<AlarmIssuedDTO>>() {});
            alarmList.forEach(alarm -> System.out.println("서버로부터 받은 알람 출력: " + alarm.getMeasurementName() + " " + alarm.getTime() + " " + alarm.getMessage()));


        } catch (IOException e) {
            System.out.println("서버 연결 오류: " + e.getMessage());
        }
    }*/

    public List<AlarmIssuedDTO> connectToServer() {
        String serverAddress = "localhost"; // 서버 주소
        int serverPort = 9221; // 서버 포트
        List<AlarmIssuedDTO> alarmList = new ArrayList<>();
        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("서버에 연결됨.");

            String json = reader.readLine(); // 서버로부터 JSON 메시지 수신
            alarmList = JsonUtil.fromJson(json, new TypeToken<List<AlarmIssuedDTO>>() {});
            alarmList.forEach(alarm -> System.out.println("서버로부터 받은 알람 출력: " + alarm.getMeasurementName() + " " + alarm.getTime() + " " + alarm.getMessage()));

        } catch (IOException e) {
            System.out.println("서버 연결 오류: " + e.getMessage());
        }

        return alarmList;
    }


}

