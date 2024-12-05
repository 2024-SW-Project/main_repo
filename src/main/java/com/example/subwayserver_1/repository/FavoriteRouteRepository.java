package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

    // 특정 사용자 ID로 즐겨찾기 조회
    List<FavoriteRoute> findByUserId(Long userId);

    // 특정 사용자 ID로 즐겨찾기 삭제
    void deleteByUserId(Long userId);

    // 특정 조건으로 즐겨찾기 조회
    @Query("SELECT f FROM FavoriteRoute f WHERE f.userId = :userId AND f.startStationName = :startStationName AND f.endStationName = :endStationName AND f.isClimateCardEligible = :isClimateCardEligible")
    List<FavoriteRoute> findByUserIdAndStations(
            @Param("userId") Long userId,
            @Param("startStationName") String startStationName,
            @Param("endStationName") String endStationName,
            @Param("isClimateCardEligible") boolean isClimateCardEligible
    );
}
