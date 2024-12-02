package com.example.subwayserver_1.service;

import com.example.subwayserver_1.entity.CalendarRoute;
import com.example.subwayserver_1.repository.CalendarRouteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final CalendarRouteRepository calendarRouteRepository;

    public CalendarService(CalendarRouteRepository calendarRouteRepository) {
        this.calendarRouteRepository = calendarRouteRepository;
    }

    public List<CalendarRoute> getCalendarDates(Long userId) {
        return calendarRouteRepository.findByUserId(userId);
    }

    public List<CalendarRoute> getRoutesByDate(Long userId, LocalDate date) {
        return calendarRouteRepository.findByUserIdAndScheduledDate(userId, date);
    }
}
