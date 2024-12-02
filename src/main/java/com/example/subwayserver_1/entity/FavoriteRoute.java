package com.example.subwayserver_1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Entity
@Table(name = "favorite_routes")
public class FavoriteRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("user_id")
    @Column(nullable = false)
    private Long userId;

    @JsonProperty("start_station_name")
    @Column(nullable = false, length = 50)
    private String startStationName;

    @JsonProperty("end_station_name")
    @Column(nullable = false, length = 50)
    private String endStationName;

    @JsonProperty("is_climate_card_eligible")
    @Column(nullable = false)
    private Boolean isClimateCardEligible;
}
