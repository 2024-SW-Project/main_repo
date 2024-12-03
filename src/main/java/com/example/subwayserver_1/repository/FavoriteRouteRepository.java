package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
    List<FavoriteRoute> findByUserId(Long userId);
    void deleteByUserId(Long userId); // 추가
    @Query("SELECT f FROM FavoriteRoute f WHERE f.userId = :userId AND f.startStationName = :startStationName AND f.endStationName = :endStationName AND f.isClimateCardEligible = :isClimateCardEligible")
    List<FavoriteRoute> findByUserIdAndStations(
            @Param("userId") Long userId,
            @Param("startStationName") String startStationName,
            @Param("endStationName") String endStationName,
            @Param("isClimateCardEligible") boolean isClimateCardEligible
    );
}
