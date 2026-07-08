package com.nv.task1.service;

import com.nv.task1.dto.EmployeeDTO;
import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Role;
import com.nv.task1.entity.User;
import com.nv.task1.repository.EmployeeRepository;
import com.nv.task1.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void saveEmployee(EmployeeDTO dto) {

        Employee employee = new Employee();

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setMobile(dto.getMobile());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        employee.setJoiningDate(dto.getJoiningDate());

        Employee savedEmployee = employeeRepository.save(employee);

        User user = new User();

        user.setUsername(dto.getEmail());
        user.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );
        user.setRole(
                Role.valueOf(dto.getRole())
        );
        user.setEmployee(savedEmployee);

        userRepository.save(user);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee Not Found"));
    }

    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee Not Found"));

        userRepository.deleteByEmployee(employee);

        employeeRepository.delete(employee);
    }

    public List<Employee> searchByName(String name) {
        return employeeRepository
                .findByNameContainingIgnoreCase(name);
    }

    public List<Employee> getByDepartment(String department) {
        return employeeRepository
                .findByDepartmentIgnoreCase(department);
    }

    public long getITCount() {
        return employeeRepository.countByDepartment("IT");
    }

    public long getHRCount() {
        return employeeRepository.countByDepartment("HR");
    }public void assignManager(Long employeeId,
                               Long managerId) {

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Employee Not Found"));

        Employee manager = employeeRepository
                .findById(managerId)
                .orElseThrow(() ->
                        new RuntimeException("Manager Not Found"));

        employee.setManager(manager);

        employeeRepository.save(employee);
    }
    public List<Employee> getTeamMembers(Long managerId) {

        Employee manager = employeeRepository
                .findById(managerId)
                .orElseThrow(() ->
                        new RuntimeException("Manager Not Found"));

        return employeeRepository.findByManager(manager);
    }


}