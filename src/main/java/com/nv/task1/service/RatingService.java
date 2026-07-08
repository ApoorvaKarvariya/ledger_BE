package com.nv.task1.service;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Rating;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final EmployeeRepository employeeRepository;

    public void giveRating(Long employeeId, Employee manager,
                           Integer rating, String comment) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Rating r = new Rating();
        r.setEmployee(employee);
        r.setRatedBy(manager);
        r.setRating(rating);
        r.setComment(comment);
        r.setDate(LocalDate.now());
        ratingRepository.save(r);
    }

    public List<Rating> getByEmployee(Employee employee) {
        return ratingRepository.findByEmployee(employee);
    }

    public List<Rating> getByManager(Employee manager) {
        return ratingRepository.findByRatedBy(manager);
    }
}