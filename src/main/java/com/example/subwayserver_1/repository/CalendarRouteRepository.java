package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.CalendarRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CalendarRouteRepository extends JpaRepository<CalendarRoute, Long> {
    List<CalendarRoute> findByUserId(Long userId);
    List<CalendarRoute> findByUserIdAndScheduledDate(Long userId, LocalDate date);
    void deleteByUserId(Long userId); // 추가

}
