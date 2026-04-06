package com.example.workload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_workload")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkload {

    @Id
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "trainerWorkload", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();

    public YearSummary getYearSummary(int year) {
        return years.stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);
    }

    public YearSummary getOrCreateYearSummary(int year) {
        YearSummary yearSummary = getYearSummary(year);
        if (yearSummary == null) {
            yearSummary = YearSummary.builder()
                    .year(year)
                    .trainerWorkload(this)
                    .build();
            years.add(yearSummary);
        }
        return yearSummary;
    }
}

