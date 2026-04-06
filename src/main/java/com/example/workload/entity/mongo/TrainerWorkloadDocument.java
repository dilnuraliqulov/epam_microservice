package com.example.workload.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workload")
@CompoundIndexes({
        @CompoundIndex(name = "trainer_name_idx", def = "{'firstName': 1, 'lastName': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkloadDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private Boolean isActive;

    @Builder.Default
    private List<YearSummaryEmbedded> years = new ArrayList<>();

    public YearSummaryEmbedded getYearSummary(int year) {
        return years.stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);
    }

    public YearSummaryEmbedded getOrCreateYearSummary(int year) {
        YearSummaryEmbedded yearSummary = getYearSummary(year);
        if (yearSummary == null) {
            yearSummary = YearSummaryEmbedded.builder()
                    .year(year)
                    .build();
            years.add(yearSummary);
        }
        return yearSummary;
    }
}

