package com.nv.task1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonIgnore  // ← ye add karo
    @OneToMany(mappedBy = "manager")
    private List<Employee> teamMembers;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;
    private String name;

    private String email;

    private String mobile;

    private String department;

    private String designation;

    private Double salary;

    private LocalDate joiningDate;


}