package com.nv.task1.repository;

import com.nv.task1.entity.DailyUpdate;
import com.nv.task1.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long> {
    List<DailyUpdate> findByEmployee(Employee employee);
    List<DailyUpdate> findByEmployee_Manager(Employee manager);
}