package com.example.subwayserver_1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "final_transfer") // 테이블 이름 설정
public class FinalTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_name") // 변경된 컬럼 이름
    private String stationName;

    @Column(name = "start_line_nm") // 변경된 컬럼 이름
    private String startLineNm;

    @Column(name = "start_way_code") // 변경된 컬럼 이름
    private int startWayCode;

    @Column(name = "ex_line_nm") // 변경된 컬럼 이름
    private String exLineNm;

    @Column(name = "ex_way_code") // 변경된 컬럼 이름
    private int exWayCode;

    @Column(name = "fast_door_location") // 변경된 컬럼 이름
    private String fastDoorLocation;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStartLineNm() {
        return startLineNm;
    }

    public void setStartLineNm(String startLineNm) {
        this.startLineNm = startLineNm;
    }

    public int getStartWayCode() {
        return startWayCode;
    }

    public void setStartWayCode(int startWayCode) {
        this.startWayCode = startWayCode;
    }

    public String getExLineNm() {
        return exLineNm;
    }

    public void setExLineNm(String exLineNm) {
        this.exLineNm = exLineNm;
    }

    public int getExWayCode() {
        return exWayCode;
    }

    public void setExWayCode(int exWayCode) {
        this.exWayCode = exWayCode;
    }

    public String getFastDoorLocation() {
        return fastDoorLocation;
    }

    public void setFastDoorLocation(String fastDoorLocation) {
        this.fastDoorLocation = fastDoorLocation;
    }
}
