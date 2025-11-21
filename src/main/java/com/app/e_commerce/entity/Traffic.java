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

    @Column(nullable = false)
    private Long pageViews = 0L;

    @Column(nullable = false)
    private Long bounceCount = 0L;

    @Column(nullable = false)
    private Long totalSessionDuration = 0L;

    @Column(nullable = false)
    private Long sessionCount = 0L;

    @Column(nullable = true)
    private String trafficSources;

    public Traffic(LocalDate date, Long visitCount) {
        this.date = date;
        this.count = visitCount;
        this.pageViews = 0L;
        this.bounceCount = 0L;
        this.totalSessionDuration = 0L;
        this.sessionCount = 0L;
    }

    /**
     * Calculate bounce rate as a percentage
     * @return bounce rate percentage or 0 if no visits
     */
    @Transient
    public Double getBounceRate() {
        if (count == 0) return 0.0;
        return (double) bounceCount / count * 100;
    }

    /**
     * Calculate average session duration in seconds
     * @return average session duration or 0 if no sessions
     */
    @Transient
    public Double getAvgSessionDuration() {
        if (sessionCount == 0) return 0.0;
        return (double) totalSessionDuration / sessionCount;
    }
}
