package com.nv.task1.repository;

import com.nv.task1.entity.Employee;
import com.nv.task1.entity.Role;
import com.nv.task1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {
    List<User> findByRole(Role role);
    void deleteByEmployee(Employee employee);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmployee_EmailIgnoreCase(String email);

}