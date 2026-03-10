package com.revworkforce.revworkforce.calendar.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.service.HolidayService;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final HolidayService holidayService;

    public CalendarController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    // 🔹 Employee + Manager View
    @GetMapping("/holidays")
    public List<Holiday> getHolidays() {
        return holidayService.getAllHolidays();
    }
}