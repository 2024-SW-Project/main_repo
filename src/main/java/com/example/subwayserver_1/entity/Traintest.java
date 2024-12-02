package com.example.subwayserver_1.entity;

import jakarta.persistence.*;

@Entity
public class Traintest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STIN_CD")
    private String stinCd;

    @Column(name = "STIN_NM")
    private String stinNm;

    @Column(name = "LN_NM")
    private String lnNm;

    private Boolean boarding;
    private Boolean alighting;
    private Boolean excluded;

    @Column(name = "both_unavailable") // 정확한 컬럼 이름 매핑
    private Boolean bothUnavailable;

    @Column(name = "only_alighting")
    private Boolean onlyAlighting;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStinCd() { return stinCd; }
    public void setStinCd(String stinCd) { this.stinCd = stinCd; }

    public String getStinNm() { return stinNm; }
    public void setStinNm(String stinNm) { this.stinNm = stinNm; }

    public String getLnNm() { return lnNm; }
    public void setLnNm(String lnNm) { this.lnNm = lnNm; }

    public Boolean getBoarding() { return boarding; }
    public void setBoarding(Boolean boarding) { this.boarding = boarding; }

    public Boolean getAlighting() { return alighting; }
    public void setAlighting(Boolean alighting) { this.alighting = alighting; }

    public Boolean getExcluded() { return excluded; }
    public void setExcluded(Boolean excluded) { this.excluded = excluded; }

    public Boolean getBothUnavailable() { return bothUnavailable; }
    public void setBothUnavailable(Boolean bothUnavailable) { this.bothUnavailable = bothUnavailable; }

    public Boolean getOnlyAlighting() { return onlyAlighting; }
    public void setOnlyAlighting(Boolean onlyAlighting) { this.onlyAlighting = onlyAlighting; }
}
