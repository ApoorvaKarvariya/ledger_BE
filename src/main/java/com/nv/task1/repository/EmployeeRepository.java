package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {


    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByDepartmentIgnoreCase(String department);
    List<Employee> findByManager(Employee manager);

    Optional<Employee> findByEmail(String email);

    long countByDepartment(String department);
}