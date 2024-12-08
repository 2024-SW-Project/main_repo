package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.FinalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinalTransferRepository extends JpaRepository<FinalTransfer, Long> {
    List<FinalTransfer> findByStationNameAndStartLineNm(String stationName, String startLineNm);
}
