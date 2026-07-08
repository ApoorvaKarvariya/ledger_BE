package com.nv.task1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {



    private String name;

    private String email;

    private String mobile;

    private String department;

    private String designation;

    private Double salary;

    private LocalDate joiningDate;

    private String password;

    private String role;
}