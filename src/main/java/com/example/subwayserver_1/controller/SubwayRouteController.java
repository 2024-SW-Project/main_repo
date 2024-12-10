package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Timemin;
import com.example.subwayserver_1.entity.Traintest;
import com.example.subwayserver_1.entity.FinalTransfer;
import com.example.subwayserver_1.repository.TimeminRepository;
import com.example.subwayserver_1.repository.TraintestRepository;
import com.example.subwayserver_1.repository.FinalTransferRepository;
import com.example.subwayserver_1.repository.FavoriteRouteRepository;
import com.example.subwayserver_1.util.JwtUtil;
import com.example.subwayserver_1.entity.FavoriteRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/subway/detail")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://namotigerta.com",
                "https://namotigerta.netlify.app"
        } // 허용할 도메인
)

public class SubwayRouteController {

    @Autowired
    private TimeminRepository timeminRepository;

    @Autowired
    private TraintestRepository traintestRepository;

    @Autowired
    private FavoriteRouteRepository favoriteRouteRepository;

    @Autowired
    private FinalTransferRepository finalTransferRepository;

    private Random random = new Random();

    @PostMapping("/favorites/check")
    public Map<String, Object> checkFavoriteRoute(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request
    ) {
        Long userId = JwtUtil.extractUserIdFromToken(token);
        String startStationName = (String) request.get("start_station_name");
        String endStationName = (String) request.get("end_station_name");
        boolean isClimateCardEligible = (boolean) request.get("is_climate_card_eligible");

        Map<String, Object> response = new HashMap<>();
        List<FavoriteRoute> favoriteRoutes = favoriteRouteRepository.findByUserIdAndStations(
                userId, startStationName, endStationName, isClimateCardEligible);

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
        Map<String, Object> routeResult = isClimateCardEligible
                ? findClimateRoute(startStationName, endStationName)
                : findRouteByStrategy(startStationName, endStationName, isClimateCardEligible, true);

        if (!routeResult.containsKey("error")) {
            result.put("data", processRoute(routeResult, startStationName, endStationName));
        } else {
            result.put("error", routeResult.get("error"));
        }

        return result;
    }

    private Map<String, Object> processRoute(Map<String, Object> routeResult, String startStationName, String endStationName) {
        Map<String, Object> data = new LinkedHashMap<>();
        String route = (String) routeResult.get("route");
        String[] routeStations = route.split(" -> ");
        List<Map<String, Object>> stationList = new ArrayList<>();
        List<Map<String, Object>> exchangeInfoList = new ArrayList<>();
        int totalTravelTime = 0;

        String previousLine = extractLine(routeStations[0]);
        String segmentStartStation = routeStations[0];
        List<String> segmentStations = new ArrayList<>();
        int segmentTime = 0;

        for (int i = 1; i < routeStations.length; i++) {
            String currentStation = routeStations[i];
            String currentLine = extractLine(currentStation);

            Optional<Timemin> segmentInfo = timeminRepository.findByDepartureAndArrivalAndLine(
                    cleanStationName(routeStations[i - 1]), cleanStationName(currentStation), previousLine);
            segmentTime += segmentInfo.map(Timemin::getWeight).orElse(0);

            int currentExpress = segmentInfo.map(Timemin::getExpress).orElse(0);

            if (i != routeStations.length - 1 && currentLine.equals(previousLine)) {
                segmentStations.add(cleanStationName(currentStation));
            }

            if (i == routeStations.length - 1 || !currentLine.equals(previousLine)) {
                Map<String, Object> stationInfo = new LinkedHashMap<>();
                stationInfo.put("start_station_name", cleanStationName(segmentStartStation));
                stationInfo.put("line_name", previousLine);
                stationInfo.put("way_code", segmentInfo.map(Timemin::getUpdown).orElse(0) == 1 ? "상행/내선" : "하행/외선");
                stationInfo.put("express", currentExpress == 1 ? "급행" : "일반");
                stationInfo.put("station_name_list", new ArrayList<>(segmentStations));
                stationInfo.put("way_station_name", cleanStationName(currentStation));
                stationInfo.put("time", segmentTime);

                stationList.add(stationInfo);
                totalTravelTime += segmentTime;

                if (!currentLine.equals(previousLine)) {
                    int walkTime = random.nextInt(5) + 1;
                    totalTravelTime += walkTime;

                    Map<String, Object> exchangeInfo = new LinkedHashMap<>();
                    exchangeInfo.put("ex_start_line_num", previousLine);
                    exchangeInfo.put("ex_end_line_num", currentLine);
                    exchangeInfo.put("ex_station_name", cleanStationName(currentStation));
                    exchangeInfo.put("exWalkTime", walkTime);

                    String transferFastTrainInfo = finalTransferRepository.findByStationNameAndStartLineNm(
                                    cleanStationName(currentStation), previousLine)
                            .stream()
                            .filter(info -> info.getExLineNm().equals(currentLine))
                            .map(FinalTransfer::getFastDoorLocation)
                            .findFirst()
                            .orElse(null);

                    exchangeInfo.put("fast_train_info", transferFastTrainInfo);
                    exchangeInfoList.add(exchangeInfo);

                    segmentStartStation = currentStation;
                    segmentStations.clear();
                    segmentTime = 0;
                    previousLine = currentLine;
                }
            }
        }

        Map<String, Object> pathInfo = new LinkedHashMap<>();
        pathInfo.put("start_station_name", cleanStationName(startStationName));
        pathInfo.put("end_station_name", cleanStationName(endStationName));
        pathInfo.put("travel_time", totalTravelTime);
        pathInfo.put("is_favorite_route", null);

        data.put("pathInfo", pathInfo);
        data.put("onStationSet", Map.of("station", stationList));
        data.put("exChangeInfoSet", Map.of("exChangeInfo", exchangeInfoList));
        return data;
    }

    private Map<String, Object> findClimateRoute(String startStationName, String endStationName) {
        List<Timemin> edges = timeminRepository.findAll();
        List<Traintest> allStations = traintestRepository.findAll();

        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        Map<String, Traintest> stationCache = new HashMap<>();
        for (Traintest station : allStations) {
            stationCache.put(cleanStationName(station.getStinNm()) + "-" + station.getLnNm(), station);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            int transferComparison = Integer.compare(a.transferCount, b.transferCount);
            if (transferComparison != 0) return transferComparison;
            return Integer.compare(a.totalWeight, b.totalWeight);
        });

        pq.add(new Node(startStationName, 0, 0, null, new ArrayList<>(), new StringBuilder(), false));
        Map<String, Integer> visited = new HashMap<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            if (current.route.isEmpty()) {
                Traintest startStation = stationCache.get(cleanStationName(startStationName) + "-" + extractLine(startStationName));
                if (startStation != null && Boolean.FALSE.equals(startStation.getBoarding())) {
                    return Collections.singletonMap("error", "출발역에서 기후동행 승차가 불가능합니다.");
                }
            }

            if (current.station.equals(endStationName)) {
                Traintest endStation = stationCache.get(cleanStationName(endStationName) + "-" + extractLine(endStationName));
                if (endStation != null && Boolean.FALSE.equals(endStation.getAlighting())) {
                    return Collections.singletonMap("error", "도착역에서 기후동행 하차가 불가능합니다.");
                }

                current.route.add(formatStation(current.previousLine, current.station, current.isExpress));
                Map<String, Object> result = new HashMap<>();
                result.put("route", String.join(" -> ", current.route));
                result.put("totalTransfers", current.transferCount);
                result.put("totalWeight", current.totalWeight);
                return result;
            }

            for (Timemin edge : graph.getOrDefault(current.station, new ArrayList<>())) {
                String nextStation = edge.getArrival();
                String nextLine = edge.getLine();
                boolean nextExpress = edge.isExpress();

                Traintest nextStationInfo = stationCache.get(cleanStationName(nextStation) + "-" + nextLine);
                if (nextStationInfo != null && Boolean.FALSE.equals(nextStationInfo.getBoarding())) {
                    continue;
                }

                int newWeight = current.totalWeight + edge.getWeight();
                int newTransferCount = current.transferCount;

                // 급행 환승 처리
                if (current.previousLine != null && (!current.previousLine.equals(nextLine) || current.isExpress != nextExpress)) {
                    if (current.isExpress && !nextExpress) {
                        // 급행에서 일반으로 갈아탈 때는 환승 가중치만 증가
                        newWeight += 1; // 환승 가중치
                    }
                    newTransferCount++;
                }

                List<String> newRoute = new ArrayList<>(current.route);
                newRoute.add(formatStation(nextLine, current.station, nextExpress));

                pq.add(new Node(nextStation, newWeight, newTransferCount, nextLine, newRoute, current.calculationDetails.append(" + ").append(edge.getWeight()), nextExpress));
            }
        }

        return Collections.singletonMap("error", "기후동행 조건을 만족하는 경로를 찾지 못했습니다.");
    }

    private Map<String, Object> findRouteByStrategy(String startStationName, String endStationName, boolean isClimateCardEligible, boolean prioritizeTransfers) {
        List<Timemin> edges = timeminRepository.findAll();

        Map<String, List<Timemin>> graph = new HashMap<>();
        for (Timemin edge : edges) {
            graph.computeIfAbsent(edge.getDeparture(), k -> new ArrayList<>()).add(edge);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            int transferComparison = Integer.compare(a.transferCount, b.transferCount);
            if (transferComparison != 0) return transferComparison;
            return Integer.compare(a.totalWeight, b.totalWeight);
        });

        pq.add(new Node(startStationName, 0, 0, null, new ArrayList<>(), new StringBuilder(), false));
        Map<String, Integer> visited = new HashMap<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.containsKey(current.station) && visited.get(current.station) <= current.totalWeight) {
                continue;
            }
            visited.put(current.station, current.totalWeight);

            if (current.station.equals(endStationName)) {
                current.route.add(formatStation(current.previousLine, current.station, current.isExpress));
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
                boolean nextExpress = edge.isExpress();

                int newWeight = current.totalWeight + edge.getWeight();
                int newTransferCount = current.transferCount;

                // 급행 환승 처리
                if (current.previousLine != null && (!current.previousLine.equals(nextLine) || current.isExpress != nextExpress)) {
                    if (current.isExpress && !nextExpress) {
                        newWeight += 1; // 환승 가중치
                    }
                    newTransferCount++;
                }

                List<String> newRoute = new ArrayList<>(current.route);
                newRoute.add(formatStation(nextLine, current.station, nextExpress));

                pq.add(new Node(nextStation, newWeight, newTransferCount, nextLine, newRoute, current.calculationDetails.append(" + ").append(edge.getWeight()), nextExpress));
            }
        }

        return Collections.singletonMap("error", "경로를 찾지 못했습니다.");
    }


    private String formatStation(String line, String station, boolean isExpress) {
        return (isExpress ? "(급행) " : "") + "(" + line + ") " + station;
    }

    private String cleanStationName(String stationInfo) {
        if (stationInfo == null || stationInfo.isEmpty()) {
            return stationInfo;
        }
        return stationInfo.replaceAll("\\(급행\\)", "").replaceAll("\\(.*?\\)", "").trim();
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
        return null;
    }

    private static class Node {
        String station;
        int totalWeight;
        int transferCount;
        String previousLine;
        List<String> route;
        StringBuilder calculationDetails;
        boolean isExpress;

        public Node(String station, int totalWeight, int transferCount, String previousLine, List<String> route, StringBuilder calculationDetails, boolean isExpress) {
            this.station = station;
            this.totalWeight = totalWeight;
            this.transferCount = transferCount;
            this.previousLine = previousLine;
            this.route = route;
            this.calculationDetails = calculationDetails;
            this.isExpress = isExpress;
        }
    }

}
