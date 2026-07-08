package com.nv.task1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "regularization_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Regularization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // The past date this regularization request is correcting
    private LocalDate date;

    private LocalTime requestedPunchIn;

    private LocalTime requestedPunchOut;

    private String reason;

    @Enumerated(EnumType.STRING)
    private RegularizationStatus status;
}
