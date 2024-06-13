package com.finedust.project.finedust.config;


import com.zaxxer.hikari.HikariConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Slf4j
@Configuration
@EnableAsync
public class RestTemplateConfig implements WebMvcConfigurer {


    @Bean
    public RestTemplate restTemplate() {
        // 커넥션 매니저 설정: 커넥션 풀을 관리
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // 최대 전체 연결 수
        connectionManager.setDefaultMaxPerRoute(50); // 경로별 최대 연결 수

        // 요청 구성 설정: 타임아웃 설정
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(20))
                .setResponseTimeout(Timeout.ofSeconds(20))
                .build();

        // CloseableHttpClient 생성: 커넥션 매니저와 요청 구성을 적용
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.ofMinutes(1)) // 비활성 연결을 1분마다 정리
                .build();

        // HttpComponentsClientHttpRequestFactory에 httpClient 설정
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);


        return restTemplate;
    }


}
