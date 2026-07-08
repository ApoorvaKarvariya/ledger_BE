package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByEmployee(Employee employee);
    List<Rating> findByRatedBy(Employee manager);
}