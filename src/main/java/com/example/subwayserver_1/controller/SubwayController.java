package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.FavoriteRoute;
import com.example.subwayserver_1.entity.CalendarRoute;
import com.example.subwayserver_1.repository.FavoriteRouteRepository;
import com.example.subwayserver_1.repository.CalendarRouteRepository;
import com.example.subwayserver_1.repository.UserDetailsRepository;
import com.example.subwayserver_1.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/subway/detail")
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시

public class SubwayController {

    private final FavoriteRouteRepository favoriteRouteRepository;
    private final CalendarRouteRepository calendarRouteRepository;
    private final UserDetailsRepository userDetailsRepository;

    public SubwayController(FavoriteRouteRepository favoriteRouteRepository,
                            CalendarRouteRepository calendarRouteRepository,
                            UserDetailsRepository userDetailsRepository) {
        this.favoriteRouteRepository = favoriteRouteRepository;
        this.calendarRouteRepository = calendarRouteRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    @PostMapping("/favorites")
    public ResponseEntity<Map<String, Object>> addFavoriteRoute(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FavoriteRoute favoriteRoute) {

        String username = validateAndExtractUsername(authHeader);
        Long authenticatedUserId = getUserIdFromUsername(username);

        if (!favoriteRoute.getUserId().equals(authenticatedUserId)) {
            throw new RuntimeException("User ID in the request does not match the authenticated user.");
        }

        FavoriteRoute savedRoute = favoriteRouteRepository.save(favoriteRoute);
        return ResponseEntity.status(201).body(Map.of(
                "message", "Route added to favorites successfully",
                "favorite_id", savedRoute.getId()
        ));
    }

    @PostMapping("/calendar")
    public ResponseEntity<Map<String, Object>> saveRouteToCalendar(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> requestBody) {

        String username = validateAndExtractUsername(authHeader);
        Long authenticatedUserId = getUserIdFromUsername(username);

        if (!authenticatedUserId.equals(((Number) requestBody.get("user_id")).longValue())) {
            throw new RuntimeException("User ID in the request does not match the authenticated user.");
        }

        CalendarRoute calendarRoute = new CalendarRoute();
        calendarRoute.setUserId(authenticatedUserId);
        calendarRoute.setStartStationName((String) requestBody.get("start_station_name"));
        calendarRoute.setEndStationName((String) requestBody.get("end_station_name"));
        calendarRoute.setIsClimateCardEligible((Boolean) requestBody.get("is_climate_card_eligible"));
        calendarRoute.setScheduledDate(LocalDate.parse((String) requestBody.get("scheduled_date")));
        calendarRoute.setDayType((Integer) requestBody.get("day_type"));
        calendarRoute.setReminderTime(LocalTime.parse((String) requestBody.get("reminder_time")));

        CalendarRoute savedRoute = calendarRouteRepository.save(calendarRoute);

        return ResponseEntity.status(201).body(Map.of(
                "message", "Route saved to calendar successfully",
                "calendar_id", savedRoute.getId()
        ));
    }

    private Long getUserIdFromUsername(String username) {
        return userDetailsRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private String validateAndExtractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return JwtUtil.extractUsername(token);
    }
}
