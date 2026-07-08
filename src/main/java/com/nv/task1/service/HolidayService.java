package com.nv.task1.service;

import com.nv.task1.entity.Holiday;
import com.nv.task1.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public List<Holiday> getAll() {
        return holidayRepository.findAllByOrderByDateAsc();
    }

    // Holidays today or in the future, soonest first
    public List<Holiday> getUpcoming() {
        return holidayRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
    }

    public Holiday getById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
    }

    public Holiday addHoliday(Holiday holiday) {
        if (holidayRepository.existsByDate(holiday.getDate())) {
            throw new RuntimeException("A holiday is already set for that date");
        }
        return holidayRepository.save(holiday);
    }

    public Holiday updateHoliday(Long id, Holiday updated) {
        Holiday existing = getById(id);

        if (!existing.getDate().equals(updated.getDate()) && holidayRepository.existsByDate(updated.getDate())) {
            throw new RuntimeException("A holiday is already set for that date");
        }

        existing.setName(updated.getName());
        existing.setDate(updated.getDate());
        existing.setDescription(updated.getDescription());
        return holidayRepository.save(existing);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
}
