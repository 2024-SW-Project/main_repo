/*
package com.example.subwayserver.controller;

import com.example.subwayserver.entity.CalendarRoute;
import com.example.subwayserver.service.CalendarService;
import com.example.subwayserver.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subway/save/calendar")
@CrossOrigin(origins = "http://localhost:5173")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public Map<String, Object> getCalendarDates(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);

        List<CalendarRoute> dates = calendarService.getCalendarDates(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("dates", dates.stream()
                .map(CalendarRoute::getScheduledDate)
                .distinct()
                .toList()));
        return response;
    }

    @GetMapping(params = "date")
    public Map<String, Object> getRoutesByDate(@RequestHeader("Authorization") String token,
                                               @RequestParam String date) {
        Long userId = extractUserIdFromToken(token);
        LocalDate scheduledDate = LocalDate.parse(date);

        List<CalendarRoute> routes = calendarService.getRoutesByDate(userId, scheduledDate);

        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("routes", routes));
        return response;
    }

    private Long extractUserIdFromToken(String token) {
        return JwtUtil.extractUserIdFromToken(token);
    }
}
*/




package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.CalendarRoute;
import com.example.subwayserver_1.service.CalendarService;
import com.example.subwayserver_1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subway/save/calendar")
@CrossOrigin(origins = "http://localhost:5173")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // 파라미터 없이 요청 시 모든 날짜 반환
    @GetMapping
    public Map<String, Object> getCalendarDates(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);

        // 특정 사용자에 저장된 모든 날짜 가져오기
        List<CalendarRoute> dates = calendarService.getCalendarDates(userId);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("dates", dates.stream()
                .map(CalendarRoute::getScheduledDate)
                .distinct()
                .toList()));
        return response;
    }

    // 특정 날짜에 대한 일정 조회
    @GetMapping(params = "date")
    public Map<String, Object> getRoutesByDate(@RequestHeader("Authorization") String token,
                                               @RequestParam String date) {
        Long userId = extractUserIdFromToken(token);
        LocalDate scheduledDate = LocalDate.parse(date);

        // 특정 날짜의 일정 가져오기
        List<CalendarRoute> routes = calendarService.getRoutesByDate(userId, scheduledDate);

        // 필요한 데이터만 필터링
        List<Map<String, Object>> filteredRoutes = routes.stream()
                .map(route -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", route.getId()); // ID 추가
                    map.put("start_station_name", route.getStartStationName());
                    map.put("end_station_name", route.getEndStationName());
                    map.put("is_climate_card_eligible", route.getIsClimateCardEligible());
                    map.put("scheduled_date", route.getScheduledDate().toString());
                    map.put("reminder_time", route.getReminderTime().toString());
                    return map;
                })
                .collect(Collectors.toList());

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("routes", filteredRoutes));
        return response;
    }
    @DeleteMapping("/delete")
    public Map<String, Object> deleteCalendarRoute(@RequestBody Map<String, Long> body) {
        // 요청 바디에서 ID 추출
        Long id = body.get("id");

        // 해당 ID로 일정 삭제
        boolean isDeleted = calendarService.deleteCalendarRoute(id);

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        if (isDeleted) {
            response.put("message", "Calendar route deleted successfully.");
            response.put("status", "success");
        } else {
            response.put("message", "Calendar route not found or could not be deleted.");
            response.put("status", "failure");
        }
        return response;
    }

    private Long extractUserIdFromToken(String token) {
        return JwtUtil.extractUserIdFromToken(token);
    }
}
