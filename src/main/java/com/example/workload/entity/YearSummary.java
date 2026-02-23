package com.example.workload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "year_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "trainerWorkload")
@ToString(exclude = "trainerWorkload")
public class YearSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_username", nullable = false)
    private TrainerWorkload trainerWorkload;

    @OneToMany(mappedBy = "yearSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<MonthlySummary> months = new ArrayList<>();

    public MonthlySummary getMonthSummary(int month) {
        return months.stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);
    }

    public MonthlySummary getOrCreateMonthSummary(int month) {
        MonthlySummary monthlySummary = getMonthSummary(month);
        if (monthlySummary == null) {
            monthlySummary = MonthlySummary.builder()
                    .month(month)
                    .trainingSummaryDuration(0)
                    .yearSummary(this)
                    .build();
            months.add(monthlySummary);
        }
        return monthlySummary;
    }
}

