package com.finedust.project.finedust;

import com.finedust.project.finedust.config.AlarmClient;
import com.finedust.project.finedust.config.AlarmServer;
import com.finedust.project.finedust.service.MainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class FinedustApplication {
	public static void main(String[] args) {
		SpringApplication.run(FinedustApplication.class, args);
	}

	@Bean
	public CommandLineRunner schedulingRunner(AlarmServer alarmServer, AlarmClient alarmClient) {
		return args -> {
			Thread serverThread = new Thread(() -> {
				try {
					alarmServer.startServer();  // 서버 시작
				} catch (IOException e) {
					System.out.println("서버 실행 중 오류 발생: " + e.getMessage());
					e.printStackTrace();
				}
			});
			serverThread.start();

			Thread clientThread = new Thread(() -> {
				try {
					Thread.sleep(5000); // 서버가 시작될 시간을 기다림
					alarmClient.connectToServer();  // 클라이언트 시작
				} catch (InterruptedException e) {
					System.out.println("클라이언트 실행 중 오류 발생: " + e.getMessage());
					e.printStackTrace();
				}
			});
			clientThread.start();
		};
	}
}
