package com.nv.task1.repository;

import com.nv.task1.entity.Attendance;
import com.nv.task1.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployee(Employee employee);
    List<Attendance> findByEmployee_Manager(Employee manager);
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);

    // Rows for a given date where the employee punched in but never punched
    // out - used by the end-of-day job to flag missed punch-outs.
    List<Attendance> findByDateAndPunchInIsNotNullAndPunchOutIsNull(LocalDate date);
}