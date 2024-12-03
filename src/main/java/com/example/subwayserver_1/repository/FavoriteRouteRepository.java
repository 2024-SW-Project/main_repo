package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
    List<FavoriteRoute> findByUserId(Long userId);
    void deleteByUserId(Long userId); // 추가

}
