package com.nv.task1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "wfh_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class WFHRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate date;

    private String reason;

    @Enumerated(EnumType.STRING)
    private WFHStatus status;
}