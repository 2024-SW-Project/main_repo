package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.Traintest;
import com.example.subwayserver_1.repository.TraintestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시

public class TraintestController {

    @Autowired
    private TraintestRepository traintestRepository;

    // 역 이름으로 검색
    @GetMapping("/traintest/{name}")

    public List<Traintest> getTraintestByName(@PathVariable String name) {
        return traintestRepository.findByStinNm(name);
    }

    // 역 이름과 호선 이름으로 검색
    @GetMapping("/traintest/{name}/{lineName}")
    public Optional<Traintest> getTraintestByLine(@PathVariable String name, @PathVariable String lineName) {
        return traintestRepository.findByStinNmAndLnNm(name, lineName);
    }
}
