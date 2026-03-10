package com.revworkforce.revworkforce.calendar.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "holiday_date")
    private LocalDate date;

    private String description;

    private boolean optionalHoliday; // true = optional leave

    // ===== Getters & Setters =====

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public boolean isOptionalHoliday() { return optionalHoliday; }

    public void setOptionalHoliday(boolean optionalHoliday) {
        this.optionalHoliday = optionalHoliday;
    }

	public void setId(Long id) {
		this.id = id;
	}
}