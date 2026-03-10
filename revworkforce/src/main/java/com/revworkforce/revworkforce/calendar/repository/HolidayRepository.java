package com.revworkforce.revworkforce.calendar.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revworkforce.revworkforce.calendar.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findAllByOrderByDateAsc();

    List<Holiday> findByDateBetween(LocalDate start, LocalDate end);
    
    List<Holiday> findByDateAfter(LocalDate date);
}