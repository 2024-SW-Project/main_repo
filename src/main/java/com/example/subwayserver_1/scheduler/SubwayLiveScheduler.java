package com.example.subwayserver_1.scheduler;

import com.example.subwayserver_1.dto.SubwayLiveResponseDto;
import com.example.subwayserver_1.service.SubwayLiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SubwayLiveScheduler {

    @Autowired
    private SubwayLiveService subwayLiveService;

    /**
     * 10초마다 실시간 지하철 데이터 조회
     */
    @Scheduled(fixedRate = 10000)
    public void fetchRealtimeSubwayData() {
        // 1호선 데이터 처리
        Map<String, Object> realtimeData1 = subwayLiveService.fetchRealtimeSubwayData("1호선", "1");
        if (realtimeData1.containsKey("groupedData")) {
            Map<String, List<SubwayLiveResponseDto>> groupedData =
                    (Map<String, List<SubwayLiveResponseDto>>) realtimeData1.get("groupedData");

            System.out.println("1호선:");
            groupedData.forEach((destination, trains) -> {
                System.out.println("종착역: " + destination);
                trains.forEach(train -> System.out.println(train));
            });
        }

        // 1호선 이외의 모든 데이터 처리
        List<String> otherLines = List.of("2호선", "3호선", "4호선", "5호선", "6호선", "7호선", "8호선", "9호선");
        for (String lineName : otherLines) {
            Map<String, Object> realtimeDataOther = subwayLiveService.fetchRealtimeSubwayData(lineName, "1");
            if (realtimeDataOther.containsKey("simpleData")) {
                List<SubwayLiveResponseDto> simpleData =
                        (List<SubwayLiveResponseDto>) realtimeDataOther.get("simpleData");

                System.out.println(lineName + ":");
                simpleData.forEach(System.out::println);
            }
        }
    }
}
