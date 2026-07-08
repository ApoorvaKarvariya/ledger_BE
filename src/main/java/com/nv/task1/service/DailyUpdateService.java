package com.nv.task1.service;

import com.nv.task1.entity.DailyUpdate;
import com.nv.task1.entity.Employee;
import com.nv.task1.entity.User;
import com.nv.task1.repository.DailyUpdateRepository;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyUpdateService {

    private final DailyUpdateRepository dailyUpdateRepository;
    private final UserRepository userRepository;

    public void saveUpdate(String updateText, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DailyUpdate update = new DailyUpdate();
        update.setEmployee(user.getEmployee());
        update.setDate(LocalDate.now());
        update.setUpdateText(updateText);
        dailyUpdateRepository.save(update);
    }

    public List<DailyUpdate> getByEmployee(Employee employee) {
        return dailyUpdateRepository.findByEmployee(employee);
    }

    public List<DailyUpdate> getByManager(Employee manager) {
        return dailyUpdateRepository.findByEmployee_Manager(manager);
    }
}