package com.nv.task1.repository;

import com.nv.task1.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findAllByOrderByDateAsc();
    List<Holiday> findByDateGreaterThanEqualOrderByDateAsc(LocalDate from);
    Optional<Holiday> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
}
