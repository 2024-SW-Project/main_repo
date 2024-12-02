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
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시

public class TimeminController {

    @Autowired
    private TimeminRepository timeminRepository;

    @GetMapping("/calculate-time/{departure}/{arrival}")

    public Map<String, Object> calculateTime(@PathVariable String departure, @PathVariable String arrival) {
        List<Timemin> edges = timeminRepository.findAll();

        // 그래프 생성
        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        // 다익스트라 알고리즘을 위한 초기 설정
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.totalWeight));
        Map<String, Integer> visited = new HashMap<>();
        pq.add(new Node(departure, 0, null, new ArrayList<>(), new StringBuilder()));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // 방문 처리
            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            // 도착 역 처리
            if (current.station.equals(arrival)) {
                current.route.add("(" + current.previousLine + ") " + current.station);
                Map<String, Object> result = new HashMap<>();
                result.put("route", String.join(" -> ", current.route));
                result.put("calculationDetails", current.calculationDetails.toString());
                result.put("totalWeight", current.totalWeight);
                return result;
            }

            // 인접 노드 탐색
            List<Timemin> neighbors = graph.getOrDefault(current.station, new ArrayList<>());
            for (Timemin edge : neighbors) {
                String nextStation = edge.getArrival();
                int newWeight = current.totalWeight + edge.getWeight();

                // 환승 여부 확인
                String transfer = "";
                StringBuilder newCalculation = new StringBuilder(current.calculationDetails);

                if (current.previousLine != null && !current.previousLine.equals(edge.getLine())) {
                    newWeight += 3; // 환승 페널티 3 추가
                    transfer = "<환승: 3분> ";

                    // 환승 가중치를 계산 순서에 추가
                    if (newCalculation.length() > 0) {
                        newCalculation.append(" + ");
                    }
                    newCalculation.append("(3)"); // 환승 가중치 강조
                }

                // 계산에 가중치를 추가
                if (newCalculation.length() > 0) {
                    newCalculation.append(" + ");
                }
                newCalculation.append(edge.getWeight());

                // 다음 노드로 이동
                List<String> newRoute = new ArrayList<>(current.route);
                if (!transfer.isEmpty()) {
                    // 환승 전역과 환승 후역을 함께 출력
                    newRoute.add("(" + current.previousLine + ") " + current.station);
                    newRoute.add(transfer + "(" + edge.getLine() + ") " + current.station);
                } else {
                    newRoute.add("(" + edge.getLine() + ") " + current.station);
                }

                pq.add(new Node(nextStation, newWeight, edge.getLine(), newRoute, newCalculation));
            }
        }

        // 경로를 찾지 못한 경우
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "No path found between " + departure + " and " + arrival);
        return errorResult;
    }

    // Node 클래스
    private static class Node {
        String station;
        int totalWeight;
        String previousLine;
        List<String> route;
        StringBuilder calculationDetails;

        public Node(String station, int totalWeight, String previousLine, List<String> route, StringBuilder calculationDetails) {
            this.station = station;
            this.totalWeight = totalWeight;
            this.previousLine = previousLine;
            this.route = route;
            this.calculationDetails = calculationDetails;
        }
    }
}
