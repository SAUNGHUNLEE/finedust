package com.finedust.project.finedust.config;

import com.finedust.project.finedust.dto.AlarmIssuedDTO;
import com.finedust.project.finedust.service.MainService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
public class AlarmServer {
    private final MainService mainService;

    @Autowired
    public AlarmServer(MainService mainService) {
        this.mainService = mainService;
    }

    public void startServer() throws IOException{
        int port = 9221;
        ServerSocket serverSocket = new ServerSocket(port);
        try(serverSocket){
            System.out.println("서버 포트 " + port + " 에서 실행중");

            while(true){
                Socket clientSocket = serverSocket.accept();
                try(clientSocket){
                    System.out.println("클라이언트 연결 성공" + clientSocket.getInetAddress().getHostAddress());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    List<AlarmIssuedDTO> alarmData  = mainService.getPmWarning();
                    System.out.println(alarmData + " 알람 모음 AlarmServer");

                    String json = JsonUtil.toJson(alarmData);
                    writer.write(json);
                    writer.newLine();
                    writer.flush();

                }catch(IOException e){
                    System.out.println("클라이언트 처리 중 오류 발생" + e.getMessage());
                }

            }
        }catch(IOException e){
            System.out.println("서버 오류 " + e.getMessage());
            e.printStackTrace();
        }
    }

}
