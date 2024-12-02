package com.example.subwayserver_1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.subwayserver_1.dto.SubwayLiveResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SubwayLiveService {

    @Value("${api.key}") // application.properties에서 API 키를 가져옴
    private String apiKey;

    private final String BASE_URL = "http://swopenAPI.seoul.go.kr/api/subway/";

    /**
     * 외부 API 호출 및 데이터 처리
     */
    public List<SubwayLiveResponseDto> fetchRealtimeSubwayData(String lineName, String updnLine) {
        try {
            // API 요청 URL 생성
            String url = BASE_URL + apiKey + "/json/realtimePosition/0/1000/" + lineName;

            // 외부 API 요청
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // 데이터 전처리
            if (response != null && response.containsKey("realtimePositionList")) {
                List<Map<String, Object>> realtimePositionList = (List<Map<String, Object>>) response.get("realtimePositionList");

                // 필터링: 상행/하행 및 특정 역
                return realtimePositionList.stream()
                        .filter(item -> updnLine.equals(item.get("updnLine")))
                        .map(item -> new SubwayLiveResponseDto(
                                (Integer) item.get("totalCount"),
                                (Integer) item.get("rowNum"),
                                (String) item.get("subwayNm"),
                                (String) item.get("statnNm"),
                                (String) item.get("trainNo"),
                                (String) item.get("updnLine"),
                                (String) item.get("statnTnm"),
                                (String) item.get("trainSttus"),
                                (String) item.get("directAt"),
                                (String) item.get("lstcarAt")
                        ))
                        .collect(Collectors.toList());
            } else {
                return List.of(); // 빈 리스트 반환
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // 오류 시 빈 리스트 반환
        }
    }
}
