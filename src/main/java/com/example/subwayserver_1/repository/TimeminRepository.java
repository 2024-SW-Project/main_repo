package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.Timemin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeminRepository extends JpaRepository<Timemin, Long> {
    // JPA Query Method 정의
    Optional<Timemin> findByDepartureAndArrivalAndLine(String departure, String arrival, String line);
}
