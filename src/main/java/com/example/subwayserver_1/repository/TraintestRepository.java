package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.Traintest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TraintestRepository extends JpaRepository<Traintest, Long> {

    List<Traintest> findByStinNm(String name);

    // name과 lineName으로 조회
    Optional<Traintest> findByStinNmAndLnNm(String name, String lineName);

    // 입력된 문자열을 포함하는 역 이름 검색
    @Query("SELECT DISTINCT t.stinNm FROM Traintest t WHERE t.stinNm LIKE %:query%")
    List<String> findStationsByQuery(@Param("query") String query);
}
