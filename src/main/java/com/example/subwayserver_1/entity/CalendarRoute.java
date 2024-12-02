// CalendarRoute.java
package com.example.subwayserver_1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "calendar_routes")
public class CalendarRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String startStationName;

    @Column(nullable = false, length = 50)
    private String endStationName;

    @Column(nullable = false)
    private Boolean isClimateCardEligible;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private Integer dayType;

    @Column(nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
