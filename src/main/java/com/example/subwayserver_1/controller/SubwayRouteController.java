package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Timemin;
import com.example.subwayserver_1.entity.Traintest;
import com.example.subwayserver_1.repository.TimeminRepository;
import com.example.subwayserver_1.repository.TraintestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


import java.util.*;

@RestController
@RequestMapping("/subway/detail")
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시
public class SubwayRouteController {

    @Autowired
    private TimeminRepository timeminRepository;
    @Autowired
    private TraintestRepository traintestRepository;

    @PostMapping("/search")
    public Map<String, Object> findDetailedRoute(@RequestBody Map<String, Object> request) {
        String startStationName = (String) request.get("start_station_name");
        String endStationName = (String) request.get("end_station_name");
        boolean isClimateCardEligible = (boolean) request.get("is_climate_card_eligible");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> routeResult;

        // 기후동행 여부에 따라 다른 알고리즘 호출
        if (isClimateCardEligible) {
            routeResult = findClimateRoute(startStationName, endStationName); // 기후동행 경로 찾기
        } else {
            routeResult = findRouteByStrategy(startStationName, endStationName, isClimateCardEligible, true); // 기존 경로 찾기
        }

        // 경로 검색 결과 처리
        if (!routeResult.containsKey("error")) {
            Map<String, Object> data = new LinkedHashMap<>();

            // PathInfo 구성
            Map<String, Object> pathInfo = new LinkedHashMap<>();
            pathInfo.put("start_station_name", cleanStationName(startStationName));
            pathInfo.put("end_station_name", cleanStationName(endStationName));
            pathInfo.put("travel_time", routeResult.get("totalWeight"));
            pathInfo.put("is_favorite_route", null);
            data.put("pathInfo", pathInfo);

            // OnStationSet 구성
            Map<String, Object> onStationSet = new LinkedHashMap<>();
            List<Map<String, Object>> stationList = new ArrayList<>();

            String route = (String) routeResult.get("route");
            String[] routeStations = route.split(" -> ");
            List<String> segmentStations = new ArrayList<>();

            String previousLine = extractLine(routeStations[0]);
            for (int i = 0; i < routeStations.length; i++) {
                String currentStation = routeStations[i];
                String currentLine = extractLine(currentStation);

                // 환승이 발생하거나 마지막 역에 도달했을 경우
                if (!previousLine.equals(currentLine) || i == routeStations.length - 1) {
                    Map<String, Object> stationInfo = new LinkedHashMap<>();
                    stationInfo.put("start_station_name", cleanStationName(segmentStations.get(0)));
                    stationInfo.put("line_name", previousLine);
                    stationInfo.put("departure_time", null);
                    stationInfo.put("way_code", null);
                    stationInfo.put("fast_train_info", null);

                    // 지나치는 역 구성
                    if (i == routeStations.length - 1) {
                        stationInfo.put("station_name_list", cleanStationNames(segmentStations.subList(1, segmentStations.size())));
                        stationInfo.put("way_station_name", cleanStationName(currentStation));
                    } else {
                        stationInfo.put("station_name_list", cleanStationNames(segmentStations.subList(1, segmentStations.size())));
                        stationInfo.put("way_station_name", cleanStationName(routeStations[i]));
                    }
                    stationInfo.put("arrival_time", null);

                    // 리스트에 추가
                    stationList.add(stationInfo);

                    // 구간 초기화
                    segmentStations.clear();
                }

                segmentStations.add(currentStation);
                previousLine = currentLine;
            }
            onStationSet.put("station", stationList);
            data.put("onStationSet", onStationSet);

            // ExChangeInfoSet 구성
            Map<String, Object> exChangeInfoSet = new LinkedHashMap<>();
            List<Map<String, Object>> exchangeInfoList = new ArrayList<>();

            for (int i = 1; i < routeStations.length; i++) {
                String prevStation = routeStations[i - 1];
                String currentStation = routeStations[i];
                String prevLine = extractLine(prevStation);
                String currentLine = extractLine(currentStation);

                if (!prevLine.equals(currentLine)) {
                    Map<String, Object> exchangeInfo = new LinkedHashMap<>();
                    exchangeInfo.put("ex_start_line_num", prevLine);
                    exchangeInfo.put("ex_end_line_num", currentLine);
                    exchangeInfo.put("ex_station_name", cleanStationName(currentStation));
                    exchangeInfo.put("exWalkTime", 3); // 기본 환승 소요 시간
                    exchangeInfoList.add(exchangeInfo);
                }
            }
            exChangeInfoSet.put("exChangeInfo", exchangeInfoList);
            data.put("exChangeInfoSet", exChangeInfoSet);

            // 결과 저장
            result.put("data", data);
        } else {
            result.put("error", routeResult.get("error")); // 오류 메시지 반환
        }

        return result;
    }

    // 역 이름에서 "(급행)"과 "(호선)"을 제거하는 메서드
    private String cleanStationName(String stationInfo) {
        if (stationInfo == null || stationInfo.isEmpty()) {
            return stationInfo; // 입력값이 없으면 그대로 반환
        }

        // "(급행)"을 제거
        stationInfo = stationInfo.replaceAll("\\(급행\\)", "").trim();

        // "(호선)" 및 특정 호선명 제거
        // "(호선)"을 포함한 모든 호선명 제거 (예: "(9호선)", "(수인분당선)", "(GTX-A)" 등)
        stationInfo = stationInfo.replaceAll("\\(.*?\\)", "").trim();

        return stationInfo; // 공백을 제거한 역 이름 반환
    }


    private String extractLine(String stationInfo) {
        if (stationInfo.startsWith("(급행)")) {
            stationInfo = stationInfo.replace("(급행)", "").trim();
        }
        int startIndex = stationInfo.indexOf("(");
        int endIndex = stationInfo.indexOf(")");
        if (startIndex != -1 && endIndex != -1) {
            return stationInfo.substring(startIndex + 1, endIndex);
        }
        return null; // 호선 정보가 없는 경우
    }

    // station_name_list를 수정하는 메서드
    private List<String> cleanStationNames(List<String> stations) {
        List<String> cleanedStations = new ArrayList<>();
        for (String station : stations) {
            cleanedStations.add(cleanStationName(station)); // 각 역명에서 "(급행)"과 "(호선)" 제거
        }
        return cleanedStations;
    }

    private Map<String, Object> findClimateRoute(String startStationName, String endStationName) {
        // 모든 Timemin 데이터와 Traintest 데이터를 한 번에 조회
        List<Timemin> edges = timeminRepository.findAll();
        List<Traintest> allStations = traintestRepository.findAll();

        // 그래프 생성
        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        // Traintest 데이터를 메모리에 캐싱
        Map<String, Traintest> stationCache = new HashMap<>();
        for (Traintest station : allStations) {
            stationCache.put(station.getStinNm() + "-" + station.getLnNm(), station);
        }

        // 나머지 알고리즘 유지
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            int transferComparison = Integer.compare(a.transferCount, b.transferCount);
            if (transferComparison != 0) return transferComparison;
            return Integer.compare(a.totalWeight, b.totalWeight);
        });

        pq.add(new Node(startStationName, 0, 0, null, new ArrayList<>(), new StringBuilder()));
        Map<String, Integer> visited = new HashMap<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // 이미 방문한 노드인지 확인
            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            // 출발역 체크
            if (current.route.isEmpty()) {
                Traintest startStation = stationCache.get(cleanStationName(startStationName) + "-" + extractLine(startStationName));
                if (startStation != null && Boolean.FALSE.equals(startStation.getBoarding())) {
                    return Collections.singletonMap("error", "출발역에서 기후동행 승차가 불가능합니다.");
                }
            }

            // 도착역 체크
            if (current.station.equals(endStationName)) {
                Traintest endStation = stationCache.get(cleanStationName(endStationName) + "-" + extractLine(endStationName));
                if (endStation != null && Boolean.FALSE.equals(endStation.getAlighting())) {
                    return Collections.singletonMap("error", "도착역에서 기후동행 하차가 불가능합니다.");
                }

                // 경로 반환
                current.route.add(formatStation(current.previousLine, current.station, false));
                Map<String, Object> result = new HashMap<>();
                result.put("route", String.join(" -> ", current.route));
                result.put("totalTransfers", current.transferCount);
                result.put("totalWeight", current.totalWeight);
                return result;
            }

            // 환승역 체크
            for (Timemin edge : graph.getOrDefault(current.station, new ArrayList<>())) {
                String nextStation = edge.getArrival();
                String nextLine = edge.getLine();

                Traintest nextStationInfo = stationCache.get(cleanStationName(nextStation) + "-" + nextLine);
                if (nextStationInfo != null && Boolean.FALSE.equals(nextStationInfo.getBoarding())) {
                    continue; // 기후동행 승차 불가능한 역 제외
                }

                int newWeight = current.totalWeight + edge.getWeight();
                int newTransferCount = current.transferCount;

                // 환승 발생 여부
                if (current.previousLine != null && !current.previousLine.equals(nextLine)) {
                    newTransferCount++;
                }

                List<String> newRoute = new ArrayList<>(current.route);
                newRoute.add(formatStation(nextLine, current.station, edge.isExpress()));

                pq.add(new Node(nextStation, newWeight, newTransferCount, nextLine, newRoute, current.calculationDetails.append(" + ").append(edge.getWeight())));
            }
        }

        return Collections.singletonMap("error", "기후동행 조건을 만족하는 경로를 찾을 수 없습니다.");
    }

    private Map<String, Object> findRouteByStrategy(String startStationName, String endStationName, boolean isClimateCardEligible, boolean prioritizeTransfers) {
        List<Timemin> edges = timeminRepository.findAll();

        // 그래프 생성
        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            int transferComparison = Integer.compare(a.transferCount, b.transferCount);
            if (transferComparison != 0) return transferComparison; // 최소환승 우선
            return Integer.compare(a.totalWeight, b.totalWeight); // 최단거리 우선
        });

        pq.add(new Node(startStationName, 0, 0, null, new ArrayList<>(), new StringBuilder()));
        Map<String, Integer> visited = new HashMap<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            if (current.station.equals(endStationName)) {
                current.route.add(formatStation(current.previousLine, current.station, false));
                Map<String, Object> result = new HashMap<>();
                result.put("start_station_name", startStationName);
                result.put("end_station_name", endStationName);
                result.put("is_climate_card_eligible", isClimateCardEligible);
                result.put("route", String.join(" -> ", current.route));
                result.put("totalTransfers", current.transferCount);
                result.put("calculationDetails", current.calculationDetails.toString());
                result.put("totalWeight", current.totalWeight);
                return result;
            }

            for (Timemin edge : graph.getOrDefault(current.station, new ArrayList<>())) {
                String nextStation = edge.getArrival();
                String nextLine = edge.getLine();

                int newWeight = current.totalWeight + edge.getWeight();
                int newTransferCount = current.transferCount;

                if (current.previousLine != null && !current.previousLine.equals(nextLine)) {
                    newTransferCount++;
                }

                List<String> newRoute = new ArrayList<>(current.route);
                newRoute.add(formatStation(nextLine, current.station, edge.isExpress()));

                pq.add(new Node(nextStation, newWeight, newTransferCount, nextLine, newRoute, current.calculationDetails.append(" + ").append(edge.getWeight())));
            }
        }

        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "경로를 찾지 못했습니다.");
        return errorResult;
    }

    private String formatStation(String line, String station, boolean isExpress) {
        return (isExpress ? "(급행) " : "") + "(" + line + ") " + station;
    }

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