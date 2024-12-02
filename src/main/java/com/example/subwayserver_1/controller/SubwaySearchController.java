package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.repository.TraintestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시

public class SubwaySearchController {

    @Autowired
    private TraintestRepository traintestRepository;

    @GetMapping("/subway/search/autocomplete")

    public Map<String, Object> getStationList(@RequestParam("query") String query) {
        Map<String, Object> result = new HashMap<>();

        if (query.length() >= 1) {
            List<String> stations = traintestRepository.findStationsByQuery(query);

            Map<String, Object> data = new HashMap<>();
            data.put("stations", stations);

            result.put("data", data);
        } else {
            result.put("data", Map.of("stations", List.of()));
        }

        return result;
    }
}
