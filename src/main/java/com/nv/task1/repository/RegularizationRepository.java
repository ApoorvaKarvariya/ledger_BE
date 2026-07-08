package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Regularization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegularizationRepository extends JpaRepository<Regularization, Long> {
    List<Regularization> findByEmployee(Employee employee);
    List<Regularization> findByEmployee_Manager(Employee manager);
}
