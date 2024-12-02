package com.example.subwayserver_1.dto;

public class SubwayLiveRequestDto {
    private String lineName; // 호선 이름
    private String updnLine; // 상행/내선(0) 또는 하행/외선(1)

    public SubwayLiveRequestDto(String lineName, String updnLine) {
        this.lineName = lineName;
        this.updnLine = updnLine;
    }

    // Getter와 Setter
    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getUpdnLine() {
        return updnLine;
    }

    public void setUpdnLine(String updnLine) {
        this.updnLine = updnLine;
    }
}
