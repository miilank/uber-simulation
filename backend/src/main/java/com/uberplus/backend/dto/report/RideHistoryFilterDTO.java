package com.uberplus.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryFilterDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String sortBy;
    private Integer page;
    private Integer size;
}