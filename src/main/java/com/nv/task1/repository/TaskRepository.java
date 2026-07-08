package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {


    List<Task> findByAssignedTo(Employee employee);

    List<Task> findByAssignedBy(Employee manager);

    List<Task> findByAssignedTo_Manager(Employee manager);
}