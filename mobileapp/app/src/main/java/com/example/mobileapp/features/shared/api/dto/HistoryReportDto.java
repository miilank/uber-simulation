package com.example.mobileapp.features.shared.api.dto;

import java.time.LocalDate;
import java.util.List;

public class HistoryReportDto {
    public static class RowElementDTO {
        public LocalDate date;
        public int numberOfRides = 0;
        public double money = 0;
        public double kms = 0;

        public RowElementDTO(LocalDate date) {
            this.date = date;
        }
    }

    public List<RowElementDTO> rows;
    public List<Double> cumulativeMoney;
    public List<Double> cumulativeKms;
    public List<Integer> cumulativeRides;

    public double totalMoney;
    public double totalKms;
    public int totalRides;
}
