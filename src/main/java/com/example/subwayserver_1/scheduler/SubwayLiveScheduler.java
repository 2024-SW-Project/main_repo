package com.example.subwayserver_1.scheduler;

import com.example.subwayserver_1.service.SubwayLiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.subwayserver_1.dto.SubwayLiveResponseDto;  // 이 라인 추가

import java.util.List;

@Component
public class SubwayLiveScheduler {

    @Autowired
    private SubwayLiveService subwayLiveService;

    @Scheduled(fixedRate = 15000) // 15초마다 실행
    public void fetchSubwayDataPeriodically() {
        try {
            // 예시로 2호선, 상행선의 실시간 데이터를 가져옵니다.
            List<SubwayLiveResponseDto> data = subwayLiveService.fetchRealtimeSubwayData("2호선", "1");

            // 콘솔에 출력
            System.out.println("실시간 데이터: ");
            data.forEach(item -> System.out.println(item)); // 리스트 출력
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
