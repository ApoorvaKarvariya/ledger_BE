package com.nv.task1.service;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.LeaveRequest;
import com.nv.task1.entity.LeaveStatus;
import com.nv.task1.entity.User;
import com.nv.task1.repository.LeaveRequestRepository;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final UserRepository userRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    // Employee applies leave
    public LeaveRequest saveLeave(LeaveRequest leaveRequest) {

        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leaveRequest);
    }

    // Admin views all leave requests
    public List<LeaveRequest> getAllLeaves() {

        return leaveRequestRepository.findAll();
    }

    // Find leave by id
    public LeaveRequest getLeaveById(Long id) {

        return leaveRequestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Leave Request Not Found"));
    }

    // Approve leave
    public void approveLeave(Long id) {

        LeaveRequest leaveRequest =
                getLeaveById(id);

        leaveRequest.setStatus(
                LeaveStatus.APPROVED
        );

        leaveRequestRepository.save(
                leaveRequest
        );
    }

    // Reject leave
    public void rejectLeave(Long id) {

        LeaveRequest leaveRequest =
                getLeaveById(id);

        leaveRequest.setStatus(
                LeaveStatus.REJECTED
        );

        leaveRequestRepository.save(
                leaveRequest
        );
    }

    // Employee views own leaves
    public List<LeaveRequest> getEmployeeLeaves(
            Employee employee) {

        return leaveRequestRepository
                .findByEmployee(employee);
    }

    public void saveLeaveRequest(
            LeaveRequest leaveRequest,
            String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User Not Found"));

        leaveRequest.setEmployee(
                user.getEmployee()
        );

        leaveRequest.setStatus(
                LeaveStatus.PENDING
        );

        leaveRequestRepository.save(
                leaveRequest
        );
    }
    public List<LeaveRequest> getLeavesByManager(Employee manager) {
        return leaveRequestRepository.findByEmployee_Manager(manager);
    }
}