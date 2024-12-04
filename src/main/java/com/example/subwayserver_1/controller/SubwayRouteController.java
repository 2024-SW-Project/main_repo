package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Timemin;
import com.example.subwayserver_1.entity.Traintest;
import com.example.subwayserver_1.repository.TimeminRepository;
import com.example.subwayserver_1.repository.TraintestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.subwayserver_1.repository.FavoriteRouteRepository;
import com.example.subwayserver_1.util.JwtUtil;
import com.example.subwayserver_1.entity.FavoriteRoute;


import java.util.*;

@RestController
@RequestMapping("/subway/detail")
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시
public class SubwayRouteController {

    @Autowired
    private TimeminRepository timeminRepository;
    @Autowired
    private TraintestRepository traintestRepository;
    @Autowired
    private FavoriteRouteRepository favoriteRouteRepository;

    @PostMapping("/favorites/check")
    public Map<String, Object> checkFavoriteRoute(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request
    ) {
        // JWT 토큰에서 사용자 ID 추출
        Long userId = JwtUtil.extractUserIdFromToken(token);

        // 요청에서 출발역, 도착역, 기후동행 여부 추출
        String startStationName = (String) request.get("start_station_name");
        String endStationName = (String) request.get("end_station_name");
        boolean isClimateCardEligible = (boolean) request.get("is_climate_card_eligible");

        // 응답 데이터 초기화
        Map<String, Object> response = new HashMap<>();

        // 즐겨찾기 테이블에서 조건에 맞는 데이터 조회
        List<FavoriteRoute> favoriteRoutes = favoriteRouteRepository.findByUserIdAndStations(
                userId,
                startStationName,
                endStationName,
                isClimateCardEligible
        );

        // 조회 결과에 따라 응답 데이터 구성
        if (!favoriteRoutes.isEmpty()) {
            response.put("favorite_route", true);
            response.put("favorite_id", favoriteRoutes.get(0).getId());
        } else {
            response.put("favorite_route", false);
            response.put("favorite_id", null);
        }

        return response;
    }


    @PostMapping("/search")
    public Map<String, Object> findDetailedRoute(@RequestBody Map<String, Object> request) {
        String startStationName = (String) request.get("start_station_name");
        String endStationName = (String) request.get("end_station_name");
        boolean isClimateCardEligible = (boolean) request.get("is_climate_card_eligible");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> routeResult;

        // 최단 거리 경로 구하기
        if (isClimateCardEligible) {
            routeResult = findClimateRoute(startStationName, endStationName);
        } else {
            routeResult = findRouteByStrategy(startStationName, endStationName, isClimateCardEligible, true);
        }

        if (!routeResult.containsKey("error")) {
            Map<String, Object> data = new LinkedHashMap<>();
            String route = (String) routeResult.get("route");
            String[] routeStations = route.split(" -> ");
            List<Map<String, Object>> stationList = new ArrayList<>();
            List<Map<String, Object>> exchangeInfoList = new ArrayList<>();
            Random random = new Random();
            int totalTravelTime = 0;

            String previousLine = extractLine(routeStations[0]);
            String segmentStartStation = routeStations[0]; // 구간 시작 역
            List<String> segmentStations = new ArrayList<>();
            int segmentTime = 0;

            for (int i = 1; i < routeStations.length; i++) {
                String currentStation = routeStations[i];
                String currentLine = extractLine(currentStation);

                // 중간역 포함 구간별 가중치 합산
                Optional<Timemin> segmentInfo = timeminRepository.findByDepartureAndArrivalAndLine(
                        cleanStationName(routeStations[i - 1]), cleanStationName(currentStation), previousLine
                );
                segmentTime += segmentInfo.map(Timemin::getWeight).orElse(0);

                // 중간 정차역 추가
                if (i != routeStations.length - 1 && currentLine.equals(previousLine)) {
                    segmentStations.add(cleanStationName(currentStation));
                }

                // 환승 발생 또는 마지막 역일 때 구간 저장
                if (i == routeStations.length - 1 || !currentLine.equals(previousLine)) {
                    Map<String, Object> stationInfo = new LinkedHashMap<>();
                    stationInfo.put("start_station_name", cleanStationName(segmentStartStation));
                    stationInfo.put("line_name", previousLine);
                    stationInfo.put("departure_time", null);
                    stationInfo.put("way_code", null);
                    stationInfo.put("fast_train_info", null);
                    stationInfo.put("station_name_list", new ArrayList<>(segmentStations)); // 중간 정차역만 포함
                    stationInfo.put("way_station_name", cleanStationName(currentStation));
                    stationInfo.put("arrival_time", null);
                    stationInfo.put("time", segmentTime); // 구간별 총 가중치

                    stationList.add(stationInfo);
                    totalTravelTime += segmentTime;

                    // 환승 정보 추가
                    if (!currentLine.equals(previousLine)) {
                        int walkTime = random.nextInt(5) + 1; // 1~5 사이 랜덤 환승 시간
                        totalTravelTime += walkTime;

                        Map<String, Object> exchangeInfo = new LinkedHashMap<>();
                        exchangeInfo.put("ex_start_line_num", previousLine);
                        exchangeInfo.put("ex_end_line_num", currentLine);
                        exchangeInfo.put("ex_station_name", cleanStationName(segmentStartStation));
                        exchangeInfo.put("exWalkTime", walkTime);

                        exchangeInfoList.add(exchangeInfo);
                    }

                    // 구간 초기화
                    segmentStartStation = currentStation; // 새로운 구간의 시작 역
                    segmentStations.clear();
                    segmentTime = 0; // 새 구간을 위한 초기화
                    previousLine = currentLine;
                }
            }

            Map<String, Object> pathInfo = new LinkedHashMap<>();
            pathInfo.put("start_station_name", cleanStationName(startStationName));
            pathInfo.put("end_station_name", cleanStationName(endStationName));
            pathInfo.put("travel_time", totalTravelTime); // 총 이동 시간
            pathInfo.put("is_favorite_route", null);

            data.put("pathInfo", pathInfo);
            data.put("onStationSet", Map.of("station", stationList));
            data.put("exChangeInfoSet", Map.of("exChangeInfo", exchangeInfoList));

            result.put("data", data);
        } else {
            result.put("error", routeResult.get("error"));
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