package com.uberplus.backend.dto.ride;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class HistoryReportDTO {
    @Data
    @NoArgsConstructor
    public static class RowElementDTO {
        private LocalDate date;
        private int numberOfRides = 0;
        private double money = 0;
        private double kms = 0;
    }

    private List<RowElementDTO> rows;
    private List<Double> cumulativeMoney;
    private List<Double> cumulativeKms;
    private List<Integer> cumulativeRides;

    private double totalMoney;
    private double totalKms;
    private int totalRides;
}
