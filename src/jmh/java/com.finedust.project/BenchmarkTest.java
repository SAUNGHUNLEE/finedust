package com.finedust.project;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgs = {"-Xms8G", "-Xmx8G"})
public class BenchmarkTest {

    private String serviceKey = "~~";


    private final static String BASE_URL = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";

    private RestTemplate restTemplate = new RestTemplate();

    // 기본 생성자 추가
    public BenchmarkTest() {
        // 필요한 초기화 코드 작성
    }



    public List<String> OpenAPiData() {

        String[] sidoNames = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주", "세종"};
        List<String> responses = new ArrayList<>();

        for(String sidoName : sidoNames){
            try {
                String encodedServiceKey = URLEncoder.encode(serviceKey, "UTF-8");
                String encodedSidoName = URLEncoder.encode(sidoName, "UTF-8");
                URI uri = UriComponentsBuilder
                        .fromUriString(BASE_URL)
                        .queryParam("serviceKey", encodedServiceKey)
                        .queryParam("returnType", "json")
                        .queryParam("sidoName", encodedSidoName)
                        .queryParam("numOfRows", "200")
                        .queryParam("ver", "1.0")
                        .build(true)
                        .toUri();

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // JSON 형식 강제
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                responses.add(response.getBody());
            } catch (RestClientException e) {
                throw new RuntimeException("API 호출 실패", e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return responses;
    }


    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTest.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(30))
                .warmupIterations(6)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(30)
                .threads(3)
                .forks(3)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void benchmarkCreateWebClient() throws UnsupportedEncodingException {
        OpenAPiData();
    }
}