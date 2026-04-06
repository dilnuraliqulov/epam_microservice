package com.example.workload.entity.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearSummaryEmbedded {

    private Integer year;

    @Builder.Default
    private List<MonthSummaryEmbedded> months = new ArrayList<>();

    public MonthSummaryEmbedded getMonthSummary(int month) {
        return months.stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);
    }

    public MonthSummaryEmbedded getOrCreateMonthSummary(int month) {
        MonthSummaryEmbedded monthlySummary = getMonthSummary(month);
        if (monthlySummary == null) {
            monthlySummary = MonthSummaryEmbedded.builder()
                    .month(month)
                    .trainingSummaryDuration(0)
                    .build();
            months.add(monthlySummary);
        }
        return monthlySummary;
    }
}

