package com.example.subwayserver_1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.subwayserver_1.dto.SubwayLiveResponseDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubwayLiveService {

    @Value("${api.key}") // application.properties에서 API 키를 가져옴
    private String apiKey;

    private final String BASE_URL = "http://swopenAPI.seoul.go.kr/api/subway/";

    /**
     * 외부 API 호출 및 데이터 처리
     */
    public Map<String, Object> fetchRealtimeSubwayData(String lineName, String updnLine) {
        try {
            // API 요청 URL 생성
            String url = BASE_URL + apiKey + "/json/realtimePosition/0/1000/" + lineName;

            // 외부 API 요청
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // 데이터 전처리
            if (response != null && response.containsKey("realtimePositionList")) {
                List<Map<String, Object>> realtimePositionList = (List<Map<String, Object>>) response.get("realtimePositionList");

                // 1호선일 경우: 종착역(statnTnm) 기준으로 데이터 분류
                if ("1호선".equals(lineName)) {
                    Map<String, List<SubwayLiveResponseDto>> groupedByDestination = realtimePositionList.stream()
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
                            .collect(Collectors.groupingBy(SubwayLiveResponseDto::getStatnTnm)); // 종착역 기준으로 그룹화
                    return Map.of("groupedData", groupedByDestination);
                }

                // 1호선이 아닐 경우: 단순 리스트 반환
                List<SubwayLiveResponseDto> filteredData = realtimePositionList.stream()
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
                return Map.of("simpleData", filteredData);
            } else {
                return Map.of("simpleData", List.of()); // 빈 리스트 반환
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("simpleData", List.of()); // 오류 시 빈 리스트 반환
        }
    }
}
