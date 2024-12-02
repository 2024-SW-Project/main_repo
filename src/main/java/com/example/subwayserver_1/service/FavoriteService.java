package com.example.subwayserver_1.service;

import com.example.subwayserver_1.entity.FavoriteRoute;
import com.example.subwayserver_1.repository.FavoriteRouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRouteRepository favoriteRouteRepository;

    public FavoriteService(FavoriteRouteRepository favoriteRouteRepository) {
        this.favoriteRouteRepository = favoriteRouteRepository;
    }

    public List<FavoriteRoute> getFavorites(Long userId) {
        // userId로 데이터 조회
        return favoriteRouteRepository.findByUserId(userId);
    }
}
