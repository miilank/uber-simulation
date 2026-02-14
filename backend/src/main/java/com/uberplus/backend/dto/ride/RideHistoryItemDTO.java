package com.uberplus.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryItemDTO {
    private Integer id;

    // frontend expects date + time string
    private String date;
    private String time;

    private String from;
    private String to;

    private String status;
    private String cancelledBy;

    private boolean panic;
    private String price;

    private LocalDateTime actualEndTime;
    private boolean alreadyRated;
}
