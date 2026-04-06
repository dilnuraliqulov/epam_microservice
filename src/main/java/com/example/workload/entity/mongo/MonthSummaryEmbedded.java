package com.example.workload.entity.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthSummaryEmbedded {

    private Integer month;

    private Integer trainingSummaryDuration;

    public void addDuration(int duration) {
        this.trainingSummaryDuration += duration;
    }

    public void subtractDuration(int duration) {
        this.trainingSummaryDuration = Math.max(0, this.trainingSummaryDuration - duration);
    }
}

