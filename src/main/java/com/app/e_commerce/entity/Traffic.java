package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "traffic",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"date"})} // đảm bảo 1 ngày chỉ có 1 record
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Traffic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private Long count = 0L;

    public Traffic(LocalDate date, Long visitCount) {
        this.date = date;
        this.count = visitCount;
    }
}
