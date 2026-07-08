package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.WFHRequest;
import com.nv.task1.entity.WFHStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface WFHRequestRepository extends JpaRepository<WFHRequest, Long> {
    List<WFHRequest> findByEmployee(Employee employee);
    List<WFHRequest> findByEmployee_Manager(Employee manager);

    // Approved WFH for a given date - used to skip marking someone ABSENT
    // on a day they're approved to work from home.
    List<WFHRequest> findByDateAndStatus(LocalDate date, WFHStatus status);
}