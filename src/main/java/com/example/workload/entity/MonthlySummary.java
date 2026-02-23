package com.example.workload.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monthly_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "yearSummary")
@ToString(exclude = "yearSummary")
public class MonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "training_summary_duration", nullable = false)
    private Integer trainingSummaryDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_summary_id", nullable = false)
    private YearSummary yearSummary;

    public void addDuration(int duration) {
        this.trainingSummaryDuration += duration;
    }

    public void subtractDuration(int duration) {
        this.trainingSummaryDuration = Math.max(0, this.trainingSummaryDuration - duration);
    }
}

