package com.nv.task1.service;

import com.nv.task1.entity.*;
import com.nv.task1.repository.UserRepository;
import com.nv.task1.repository.WFHRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WFHService {

    private final WFHRequestRepository wfhRequestRepository;
    private final UserRepository userRepository;

    public void applyWFH(WFHRequest wfhRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        wfhRequest.setEmployee(user.getEmployee());
        wfhRequest.setStatus(WFHStatus.PENDING);
        wfhRequestRepository.save(wfhRequest);
    }

    public List<WFHRequest> getByEmployee(Employee employee) {
        return wfhRequestRepository.findByEmployee(employee);
    }

    public List<WFHRequest> getByManager(Employee manager) {
        return wfhRequestRepository.findByEmployee_Manager(manager);
    }

    // Company-wide - includes a manager's own WFH requests, which have no
    // manager above them to approve otherwise (same reasoning as Regularization).
    public List<WFHRequest> getAll() {
        return wfhRequestRepository.findAll();
    }

    public void approve(Long id) {
        WFHRequest r = wfhRequestRepository.findById(id).orElseThrow();
        r.setStatus(WFHStatus.APPROVED);
        wfhRequestRepository.save(r);
    }

    public void reject(Long id) {
        WFHRequest r = wfhRequestRepository.findById(id).orElseThrow();
        r.setStatus(WFHStatus.REJECTED);
        wfhRequestRepository.save(r);
    }
}