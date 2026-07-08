package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.LeaveRequest;
import com.nv.task1.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByEmployee_Manager(Employee manager);

    // Approved leaves whose range covers the given date - used to skip
    // marking someone ABSENT if they're on approved leave that day.
    List<LeaveRequest> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LeaveStatus status, LocalDate start, LocalDate end);
}