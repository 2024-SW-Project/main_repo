package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.service.FavoriteService;
import com.example.subwayserver_1.util.JwtUtil;
import com.example.subwayserver_1.entity.FavoriteRoute;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subway/save/favorite")
@CrossOrigin(origins = "http://localhost:5173")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // 유저 ID로 즐겨찾기 데이터 가져오기
    @GetMapping
    public Map<String, Object> getFavorites(@RequestParam("user_id") Long userId) {
        // 데이터 가져오기
        List<FavoriteRoute> favorites = favoriteService.getFavorites(userId);

        // 매핑 확인
        List<Map<String, Object>> formattedFavorites = favorites.stream().map(favorite -> {
            Map<String, Object> map = new HashMap<>();
            map.put("start_station_name", favorite.getStartStationName());
            map.put("end_station_name", favorite.getEndStationName());
            map.put("is_climate_card_eligible", favorite.getIsClimateCardEligible());
            return map;
        }).collect(Collectors.toList());

        // 결과 반환
        Map<String, Object> response = new HashMap<>();
        response.put("data", Map.of("favorites", formattedFavorites));
        return response;
    }
}

