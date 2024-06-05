package com.finedust.project.finedust.config;


import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Configuration
@EnableAsync
public class WebConfig implements WebMvcConfigurer {
    private final static String BASE_URL = "https://apis.data.go.kr";


    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request Headers: {}", clientRequest.headers());
            log.info("Request Method: {}", clientRequest.method());
            log.info("Request URI: {}", clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("응답 코드: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public WebClient webClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(BASE_URL);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // 서비스 키를 제외한 값만 인코딩
        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(BASE_URL)
                .filter(logRequest())
                .filter(logResponse())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    /*   @Override
       public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
           ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
           taskExecutor.setCorePoolSize(3); // 기본 스레드 수
           taskExecutor.setMaxPoolSize(8); // 최대 스레드 수
           taskExecutor.setQueueCapacity(20); // 큐 용량
           taskExecutor.initialize();
           configurer.setTaskExecutor(taskExecutor);
       }
   */



}
