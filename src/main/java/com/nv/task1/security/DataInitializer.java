package com.nv.task1.security;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Role;
import com.nv.task1.entity.User;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Admin user created (username: admin)");
        }

        if (userRepository.findByUsername("manager").isEmpty()) {
            // Pehle Employee banao
            Employee managerEmp = new Employee();
            managerEmp.setName("Default Manager");
            managerEmp.setEmail("manager@company.com");
            managerEmp.setMobile("9999999999");
            managerEmp.setDepartment("IT");
            managerEmp.setDesignation("Manager");
            managerEmp.setSalary(80000.0);
            managerEmp.setJoiningDate(LocalDate.now());
            employeeRepository.save(managerEmp);

            // Phir User banao — employee link karo
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(Role.ROLE_MANAGER);
            manager.setEmployee(managerEmp);
            userRepository.save(manager);
            System.out.println("✅ Manager Created with Employee");
        }
    }
}