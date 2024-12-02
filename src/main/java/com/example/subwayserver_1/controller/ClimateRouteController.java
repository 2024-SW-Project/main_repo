package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Traintest;
import com.example.subwayserver_1.entity.Timemin;
import com.example.subwayserver_1.repository.TraintestRepository;
import com.example.subwayserver_1.repository.TimeminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;

@RestController
public class ClimateRouteController {

    @Autowired
    private TraintestRepository traintestRepository;

    @Autowired
    private TimeminRepository timeminRepository;

    @GetMapping("/climate-route/{departure}/{arrival}")
    public Map<String, Object> getClimateRoute(@PathVariable String departure, @PathVariable String arrival) {
        Map<String, Object> result = new HashMap<>();

        // 출발역 확인
        List<Traintest> departureOptions = traintestRepository.findByStinNm(departure);
        System.out.println("Departure Options: " + departureOptions);
        for (Traintest option : departureOptions) {
            System.out.println("Station Name: " + option.getStinNm() + ", Boarding: " + option.getBoarding());
        }
        if (departureOptions.isEmpty() || departureOptions.stream().noneMatch(option -> Boolean.TRUE.equals(option.getBoarding()))) {
            result.put("message", "출발역에서 기후동행 카드 사용이 불가능합니다: " + departure);
            return result;
        }

        // 도착역 확인
        List<Traintest> arrivalOptions = traintestRepository.findByStinNm(arrival);
        System.out.println("Arrival Options: " + arrivalOptions);
        for (Traintest option : arrivalOptions) {
            System.out.println("Station Name: " + option.getStinNm() + ", Alighting: " + option.getAlighting());
        }
        if (arrivalOptions.isEmpty() || arrivalOptions.stream().noneMatch(option -> Boolean.TRUE.equals(option.getAlighting()))) {
            result.put("message", "도착역에서 기후동행 카드 사용이 불가능합니다: " + arrival);
            return result;
        }

        // 그래프 생성
        List<Timemin> edges = timeminRepository.findAll();
        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        // 다익스트라 알고리즘으로 최단 경로 계산
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
        result.put("message", "가능한 경로를 찾을 수 없습니다.");
        return result;
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
