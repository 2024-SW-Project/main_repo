package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Timemin;
import com.example.subwayserver_1.repository.TimeminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;

@RestController
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://namotigerta.com",
                "https://namotigerta.netlify.app"
        } // 허용할 도메인
)


public class MinTransferRouteController {

    @Autowired
    private TimeminRepository timeminRepository;

    @GetMapping("/min-transfer-route/{departure}/{arrival}")
    public Map<String, Object> findMinTransferRoute(@PathVariable String departure, @PathVariable String arrival) {
        List<Timemin> edges = timeminRepository.findAll();

        // 그래프 생성
        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        // 다익스트라 알고리즘 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            int transferComparison = Integer.compare(a.transferCount, b.transferCount);
            if (transferComparison != 0) return transferComparison;
            return Integer.compare(a.totalWeight, b.totalWeight);
        });

        Map<String, Integer> visited = new HashMap<>();
        pq.add(new Node(departure, 0, 0, null, new ArrayList<>(), new StringBuilder()));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // 방문 처리
            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            // 도착 역 처리
            if (current.station.equals(arrival)) {
                current.route.add(formatStation(current.previousLine, current.station, false));
                Map<String, Object> result = new HashMap<>();
                result.put("route", String.join(" -> ", current.route));
                result.put("calculationDetails", current.calculationDetails.toString());
                result.put("totalWeight", current.totalWeight);
                result.put("totalTransfers", current.transferCount);
                return result;
            }

            // 인접 노드 탐색
            List<Timemin> neighbors = graph.getOrDefault(current.station, new ArrayList<>());
            for (Timemin edge : neighbors) {
                String nextStation = edge.getArrival();
                int newWeight = current.totalWeight + edge.getWeight();
                int newTransferCount = current.transferCount;

                // 환승 여부 확인
                String transfer = "";
                StringBuilder newCalculation = new StringBuilder(current.calculationDetails);

                if (current.previousLine != null && !current.previousLine.equals(edge.getLine())) {
                    newTransferCount++; // 환승 횟수 증가
                    transfer = "<환승: 3분> ";
                }

                // 계산 기록에 가중치를 추가
                if (newCalculation.length() > 0) {
                    newCalculation.append(" + ");
                }
                newCalculation.append(edge.getWeight());

                // 다음 노드로 이동
                List<String> newRoute = new ArrayList<>(current.route);
                newRoute.add(formatStation(edge.getLine(), current.station, edge.isExpress()));
                if (!transfer.isEmpty()) {
                    newRoute.add(transfer + formatStation(edge.getLine(), current.station, edge.isExpress()));
                }

                pq.add(new Node(nextStation, newWeight, newTransferCount, edge.getLine(), newRoute, newCalculation));
            }
        }

        // 경로를 찾지 못한 경우
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "No path found between " + departure + " and " + arrival);
        return errorResult;
    }

    // 역 포맷팅 유틸 메서드
    private String formatStation(String line, String station, boolean isExpress) {
        return (isExpress ? "(급행) " : "") + "(" + line + ") " + station;
    }

    // Node 클래스
    private static class Node {
        String station;
        int totalWeight;
        int transferCount;
        String previousLine;
        List<String> route;
        StringBuilder calculationDetails;

        public Node(String station, int totalWeight, int transferCount, String previousLine, List<String> route, StringBuilder calculationDetails) {
            this.station = station;
            this.totalWeight = totalWeight;
            this.transferCount = transferCount;
            this.previousLine = previousLine;
            this.route = route;
            this.calculationDetails = calculationDetails;
        }
    }
}
