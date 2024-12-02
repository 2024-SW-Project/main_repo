package com.example.subwayserver_1.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "totalCount", "rowNum", "subwayNm", "statnNm",
        "trainNo", "updnLine", "statnTnm", "trainSttus",
        "directAt", "lstcarAt"
})
public class SubwayLiveResponseDto {

    private int totalCount;
    private int rowNum;
    private String subwayNm;
    private String statnNm;
    private String trainNo;
    private String updnLine;
    private String statnTnm;
    private String trainSttus;
    private String directAt;
    private String lstcarAt;

    // Constructor
    public SubwayLiveResponseDto(
            int totalCount,
            int rowNum,
            String subwayNm,
            String statnNm,
            String trainNo,
            String updnLine,
            String statnTnm,
            String trainSttus,
            String directAt,
            String lstcarAt
    ) {
        this.totalCount = totalCount;
        this.rowNum = rowNum;
        this.subwayNm = subwayNm;
        this.statnNm = statnNm;
        this.trainNo = trainNo;
        this.updnLine = updnLine;
        this.statnTnm = statnTnm;
        this.trainSttus = trainSttus;
        this.directAt = directAt;
        this.lstcarAt = lstcarAt;
    }

    // Getters and Setters
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getSubwayNm() {
        return subwayNm;
    }

    public void setSubwayNm(String subwayNm) {
        this.subwayNm = subwayNm;
    }

    public String getStatnNm() {
        return statnNm;
    }

    public void setStatnNm(String statnNm) {
        this.statnNm = statnNm;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getUpdnLine() {
        return updnLine;
    }

    public void setUpdnLine(String updnLine) {
        this.updnLine = updnLine;
    }

    public String getStatnTnm() {
        return statnTnm;
    }

    public void setStatnTnm(String statnTnm) {
        this.statnTnm = statnTnm;
    }

    public String getTrainSttus() {
        return trainSttus;
    }

    public void setTrainSttus(String trainSttus) {
        this.trainSttus = trainSttus;
    }

    public String getDirectAt() {
        return directAt;
    }

    public void setDirectAt(String directAt) {
        this.directAt = directAt;
    }

    public String getLstcarAt() {
        return lstcarAt;
    }

    public void setLstcarAt(String lstcarAt) {
        this.lstcarAt = lstcarAt;
    }
}
