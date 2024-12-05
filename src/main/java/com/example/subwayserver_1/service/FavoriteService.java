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
    // 특정 ID로 즐겨찾기 삭제
    public boolean deleteFavorite(Long id) {
        if (favoriteRouteRepository.existsById(id)) { // ID 존재 여부 확인
            favoriteRouteRepository.deleteById(id);   // 해당 ID 삭제
            return true;
        }
        return false; // ID가 없으면 false 반환
    }
}
