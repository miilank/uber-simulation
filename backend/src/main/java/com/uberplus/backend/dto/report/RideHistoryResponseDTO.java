package com.uberplus.backend.dto.report;

import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.dto.ride.RideHistoryItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryResponseDTO {
    private List<RideHistoryItemDTO> rides;
    private long total;
    private int page;
    private int size;
}
