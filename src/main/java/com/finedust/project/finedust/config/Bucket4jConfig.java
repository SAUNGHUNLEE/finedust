package com.finedust.project.finedust.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
@Configuration
@EnableAsync
public class Bucket4jConfig implements WebMvcConfigurer {


    @Bean
    public Bucket bucket(){
        // 토큰 리필 - 10초에 10개의 요청을 리필
        Refill refill = Refill.intervally(20, Duration.ofSeconds(10));
        // 트래픽 제한 - 최대 10개의 토큰을 가짐
        Bandwidth limit = Bandwidth.classic(500, refill);

        //bucket 생성
        Bucket bucket = Bucket.builder().addLimit(limit).build();
        return bucket;
    }


}
