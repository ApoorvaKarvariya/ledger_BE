package com.nv.task1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ratings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "rated_by")
    private Employee ratedBy;

    private Integer rating; // 1-5

    private String comment;

    private LocalDate date;
}