package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.dto.SubwayLiveResponseDto;
import com.example.subwayserver_1.service.SubwayLiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시
public class SubwayLiveController {

    @Autowired
    private SubwayLiveService subwayLiveService;

    /**
     * 실시간 지하철 데이터 조회
     */
    @GetMapping("/subway/live")
    public ResponseEntity<Map<String, Object>> getRealtimeSubwayData(
            @RequestParam("line_name") String lineName,
            @RequestParam("updn_line") String updnLine) {
        // Service에서 필터링된 데이터 반환
        Map<String, Object> realtimeSubwayData = subwayLiveService.fetchRealtimeSubwayData(lineName, updnLine);

        // 데이터를 "data" 키로 감싸서 반환
        return ResponseEntity.ok(Map.of("data", realtimeSubwayData));
    }
}
