package com.nv.task1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_updates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DailyUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate date;

    @Column(length = 1000)
    private String updateText;
}