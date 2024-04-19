package com.finedust.project.finedust.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finedust.project.finedust.dto.ResponseDTO;
import com.finedust.project.finedust.persistence.AirQualityRepository;
import com.finedust.project.finedust.service.ApartmentDetailService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/detail")
public class ApartmentDetailController {
    @Value("${openApi.decodeServiceKey}")
    private String serviceKey;
    private final ApartmentDetailService apartmentDetailService;
    private final AirQualityRepository airQualityRepository;

    public ApartmentDetailController(ApartmentDetailService apartmentDetailService, AirQualityRepository airQualityRepository) {
        this.apartmentDetailService = apartmentDetailService;
        this.airQualityRepository = airQualityRepository;
    }
    @Value("${kakao.RESTAPI}")
    private String APIKey;

    //api/get.html 연결
    @GetMapping("get")
    public String replace() {

        return "get";
    }

 /*   @ResponseBody
    @PostMapping(value = "get", produces = "application/json;charset=utf-8")
    public Flux<String> getData(@RequestParam("sidoName") String sidoName) throws UnsupportedEncodingException {

        String encodeServiceKey = URLEncoder.encode(serviceKey, "UTF-8");


        return apartmentDetailService.webClient().get()
                .uri(uriBuilder -> uriBuilder.path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                        .queryParam("serviceKey", encodeServiceKey)
                        .queryParam("returnType", "json")
                        .queryParam("sidoName", sidoName)
                        .queryParam("numOfRows", 20)
                        .build(true))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(System.out::println);
    }*/

/*    @ResponseBody
    @PostMapping(value = "get", produces = "application/json;charset=utf-8")
    public Flux<String> getData(@RequestParam("sidoName") String sidoName) throws UnsupportedEncodingException {
        String fullUri = apartmentDetailService.getBaseUrl() + "/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";
        String encodeServiceKey = URLEncoder.encode(serviceKey, "UTF-8");
        String encodeSidoName = URLEncoder.encode(sidoName,"UTF-8");
        URI uri = UriComponentsBuilder.fromUriString(fullUri)
                .queryParam("serviceKey", encodeServiceKey)
                .queryParam("returnType", "json")
                .queryParam("sidoName", encodeSidoName)
                .queryParam("numOfRows", 20)
                .build(true)
                .toUri();
        log.info("컨트롤러 경로: {}", uri);  // 로그에 URI 출력

        return apartmentDetailService.webClient().get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(System.out::println);
    }*/
/*
    @ResponseBody
    @PostMapping(value = "get", produces = "application/json;charset=utf-8")
    public Flux<String> getData(@RequestParam("sidoName") String sidoName) throws UnsupportedEncodingException {

        String encodeSidoName = URLEncoder.encode(sidoName,"UTF-8");

        Flux<String> responseFlux = apartmentDetailService.webClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                        .queryParam("serviceKey", serviceKey) // 이미 인코딩된 serviceKey를 사용
                        .queryParam("returnType", "json")
                        .queryParam("sidoName", encodeSidoName) // sidoName은 여기서 인코딩해도 되고, 미리 인코딩해도 됨
                        .queryParam("numOfRows", 50)
                        .build())
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(System.out::println);

        return responseFlux;
    }*/



    @PostMapping(value = "/getLocationInfo")
    public Flux<String> getLocalInfo(@RequestParam("lon")Double lon , @RequestParam("lat") Double lat,HttpSession session){
        Map<String,String> regionInfo = apartmentDetailService.getAddressFromCoords(lon,lat);
        System.out.println(regionInfo + "시 + 구 정보");

        String cityName = regionInfo.get("city");
        String regionName = regionInfo.get("region");

        List<JsonNode> allData = (List<JsonNode>) session.getAttribute("data");
        if (allData == null) {
            return Flux.error(new RuntimeException("세션에 데이터가 저장되지 않음"));
        }
        return Flux.fromIterable(allData)
                .flatMap(item -> Flux.fromIterable(item.path("response").path("body").path("items")))
                .filter(item -> {
                    // 도시명과 구 이름이 모두 일치하는 데이터만 필터링
                    String itemCityName = item.path("sidoName").asText().trim();
                    String itemRegionName = item.path("stationName").asText().trim();
                    return itemCityName.equalsIgnoreCase(cityName.trim()) && itemRegionName.equalsIgnoreCase(regionName.trim());
                })
                .map(this::dataResult);

    }

   @ResponseBody
    @PostMapping(value = "/get", produces = "application/json;charset=utf-8")
    public Flux<ResponseDTO.AirQualityData> getRegionInfo(@RequestParam("sidoName") String sidoName, HttpSession httpSession) throws UnsupportedEncodingException {

        String encodeSidoName = URLEncoder.encode(sidoName, "UTF-8");

        return apartmentDetailService.webClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "json")
                        .queryParam("sidoName", encodeSidoName)
                        .queryParam("numOfRows", 100)
                        .build())
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .subscribeOn(Schedulers.boundedElastic()) // I/O 작업을 위한 스케줄러 설정
                .flatMap(apartmentDetailService::saveOpenApiData)
                .doOnNext(aq -> System.out.println(aq + " DB에 저장 완료"))
                .doOnError(error -> log.error("데이터 저장 에러: ", error));
    }


/*    @PostMapping(value = "/getRegion", produces = "application/json;charse t=utf-8")
    @ResponseBody
    public Mono<ResponseDTO.AirQualityData> getSpecificRegionData(@RequestParam("regionName") String regionName) {
        return Mono.justOrEmpty(airQualityRepository.findByRegionName(regionName))
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 I/O를 위한 별도의 스레드에서 실행(스레드분리)
                .map(aq -> ResponseDTO.AirQualityData.builder()
                        .sidoName(aq.getSidoName())
                        .stationName(aq.getStationName())
                        .dataTime(aq.getDataTime())
                        .pm10Value(aq.getPm10Value())
                        .pm10Grade(aq.getPm10Grade())
                        .pm25Value(aq.getPm25Value())
                        .pm25Grade(aq.getPm25Grade())
                        .build())
                .doOnNext(aq -> System.out.println(aq + " 조회 완료"))
                .doOnError(error -> log.error("데이터 조회 에러: ", error));
    }*/

    //pm25는 왜 안나오는지 모르겠음. 전제 openapi에서 긁어와도 안나옴.
    //일단 무시하고, 메인화면에 openapi에서 가져온 데이터 띄우는거 해보기
    //성공했고, 이제 openapi에서 db갱신되면 내 db에서도 갱신되게 스케줄러 사용해보기
    @PostMapping(value = "/getRegionData", produces = "application/json;charset=utf-8")
    public Mono<ResponseDTO.AirQualityData> getRegionData(@RequestBody Map<String,Double> coords) {
        // 좌표 추출
        Double longitude = coords.get("longitude");
        Double latitude = coords.get("latitude");
        Map<String, String> addressDetails = apartmentDetailService.getAddressFromCoords(longitude, latitude);
        System.out.println(addressDetails + "위도 경도 = 시,구");
        String regionName = addressDetails.get("region");

        return Mono.justOrEmpty(airQualityRepository.findByRegionName(regionName))
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 I/O를 위한 별도의 스레드에서 실행(스레드분리)
                .map(aq -> ResponseDTO.AirQualityData.builder()
                        .sidoName(aq.getSidoName())
                        .stationName(aq.getStationName())
                        .dataTime(aq.getDataTime())
                        .pm10Value(aq.getPm10Value())
                        .pm10Grade(aq.getPm10Grade())
                        .pm25Value(aq.getPm25Value())
                        .pm25Grade(aq.getPm25Grade())
                        .build())
                .doOnNext(aq -> System.out.println(aq + " 조회 완료"))
                .doOnError(error -> log.error("데이터 조회 에러: ", error));
    }

    //구 이름이 필터링이 완료되면, 해당 구에 속한 상세 정보들
    private String dataResult(JsonNode item){
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("지역", item.path("sidoName").asText());
        result.put("측정소", item.path("stationName").asText());
        result.put("일시", item.path("dataTime").asText());
        result.put("미세먼지농도", item.path("pm10Value").asText().isEmpty() ? "데이터 없음" : item.path("pm10Value").asText());
        result.put("미세먼지 등급", item.path("pm10Grade").asText().isEmpty() ? "데이터 없음" : item.path("pm10Grade").asText());
        result.put("초미세먼지농도", item.path("pm25Value").asText().isEmpty() ? "데이터 없음" : item.path("pm25Value").asText());
        result.put("초미세먼지 등급", item.path("pm25Grade").asText().isEmpty() ? "데이터 없음" : item.path("pm25Grade").asText());

        return result.toString();
    }


    //요청 보내기
/*
   @ResponseBody
    @PostMapping(value = "get", produces = "application/json;charset=utf-8")
    public String getData(@RequestParam("sidoName") String sidoName) throws Exception {
        //요청 url 전달
        // url = 요청주소 + ?serviceKey="+serviceKey
        String url = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?serviceKey="+serviceKey;
        url += "&returnType=json"; //json으로 파라미터 보내기
        url += "&sidoName="+ URLEncoder.encode(sidoName,"UTF-8"); //필수 요청변수(서비스키, 시도명), 요청시 전달값 중 한글이 있을 경우 encoding 해야함
        url += "&numOfRows="+10000;	//한 페이지 결과 수(옵션 변수)

        //단순한 문자열로 정의한 url을 자바에서 활용할 수 있는 객체로 변환
        URL requestURL = new URL(url);
        //목적지로 향하는 다리 건설
        HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();

        conn.setRequestMethod("GET");	//GET방식으로 요청

        //OpenAPI 서버로 요청 후 입력 스트림을 통해 응답데이터 읽어들이기
        //conn 다리가 건설되어 있는 목적지로부터 데이터를 읽어와야 함
        //1. conn 목적지로부터 inputStream 생성(conn.getInputStream())
        InputStream is = conn.getInputStream();
        //2. 생성된 InputStream을 이용하기 위한 객체 생성(new InputStreamReader())
        InputStreamReader isr = new InputStreamReader(is);

        //3. InputStreamReader 객체보다 편한 BufferedReader 사용을 위해 객체 생성
        BufferedReader br = new BufferedReader(isr) ;

        //생성된 BufferedReader를 이용해서 데이터를 읽고 활용하기
        String result = "";
        String line = "";
        while(true) {
            line = br.readLine();
            if(line == null) {break;}
            result += line;

        }
        System.out.println(result);

        //사용한 스트림 반납
        br.close();
        conn.disconnect();
        return result; //String으로 했기 때문에 리턴 null 달아줌
    }

*/


}
