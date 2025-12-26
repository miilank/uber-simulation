package com.uberplus.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class    RideReportDTO {
    private String period;
    private int totalRides;
    private int completedRides;
    private double totalKm;
    private double totalPrice;
    private double averageRating;
    private Map<LocalDate, Integer> ridesPerDay;
}
