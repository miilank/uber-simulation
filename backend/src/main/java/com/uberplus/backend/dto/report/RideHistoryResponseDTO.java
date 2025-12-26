package com.uberplus.backend.dto.report;

import com.uberplus.backend.dto.ride.RideDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryResponseDTO {
    private List<RideDTO> rides;
    private long total;
    private int page;
    private int size;
}
