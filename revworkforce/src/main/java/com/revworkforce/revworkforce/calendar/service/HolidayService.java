package com.revworkforce.revworkforce.calendar.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.revworkforce.revworkforce.calendar.entity.Holiday;
import com.revworkforce.revworkforce.calendar.repository.HolidayRepository;

@Service
public class HolidayService {

    private final HolidayRepository repository;

    public HolidayService(HolidayRepository repository) {
        this.repository = repository;
    }

    // 🔹 Admin adds holiday
    public Holiday addHoliday(Holiday holiday) {
        return repository.save(holiday);
    }

    // 🔹 Admin updates holiday
    public Holiday updateHoliday(Long id, Holiday updated) {

        Holiday holiday = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));

        holiday.setName(updated.getName());
        holiday.setDate(updated.getDate());
        holiday.setDescription(updated.getDescription());
        holiday.setOptionalHoliday(updated.isOptionalHoliday());

        return repository.save(holiday);
    }

    // 🔹 Admin delete
    public void deleteHoliday(Long id) {
        repository.deleteById(id);
    }

    // 🔹 View holidays
    public List<Holiday> getAllHolidays() {
        return repository.findAllByOrderByDateAsc();
    }
    
    public List<Holiday> getUpcomingHolidays() {
        return repository.findByDateAfter(LocalDate.now());
    }
}