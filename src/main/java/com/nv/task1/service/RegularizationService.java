package com.nv.task1.service;

import com.nv.task1.entity.*;
import com.nv.task1.repository.AttendanceRepository;
import com.nv.task1.repository.RegularizationRepository;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegularizationService {

    private final RegularizationRepository regularizationRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    // Employee (or a manager, for their own attendance) raises a regularization request
    public void applyRegularization(Regularization regularization, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        regularization.setEmployee(user.getEmployee());
        regularization.setStatus(RegularizationStatus.PENDING);
        regularizationRepository.save(regularization);
    }

    public List<Regularization> getByEmployee(Employee employee) {
        return regularizationRepository.findByEmployee(employee);
    }

    // Manager: requests raised by their direct team
    public List<Regularization> getByManager(Employee manager) {
        return regularizationRepository.findByEmployee_Manager(manager);
    }

    // Admin: every regularization request in the company
    public List<Regularization> getAll() {
        return regularizationRepository.findAll();
    }

    public Regularization getById(Long id) {
        return regularizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regularization Request Not Found"));
    }

    // Approving a request also corrects (or creates) the underlying attendance record
    // for that date, so the fixed punch times actually show up in attendance history.
    public void approve(Long id) {
        Regularization r = getById(id);
        r.setStatus(RegularizationStatus.APPROVED);
        regularizationRepository.save(r);

        Optional<Attendance> existing =
                attendanceRepository.findByEmployeeAndDate(r.getEmployee(), r.getDate());

        Attendance attendance = existing.orElseGet(Attendance::new);
        attendance.setEmployee(r.getEmployee());
        attendance.setDate(r.getDate());

        if (r.getRequestedPunchIn() != null) {
            attendance.setPunchIn(r.getRequestedPunchIn());
        }
        if (r.getRequestedPunchOut() != null) {
            attendance.setPunchOut(r.getRequestedPunchOut());
        }
        if (attendance.getStatus() == null
                || attendance.getStatus() == AttendanceStatus.ABSENT
                || attendance.getStatus() == AttendanceStatus.MISSED_PUNCHOUT) {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        attendanceRepository.save(attendance);
    }

    public void reject(Long id) {
        Regularization r = getById(id);
        r.setStatus(RegularizationStatus.REJECTED);
        regularizationRepository.save(r);
    }
}
