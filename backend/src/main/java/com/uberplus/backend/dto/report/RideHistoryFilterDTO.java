package com.uberplus.backend.dto.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "Page must be >= 0")
    private Integer page;
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must be at most 100")
    private Integer size;
}