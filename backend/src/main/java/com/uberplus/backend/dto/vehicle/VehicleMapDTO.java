package com.uberplus.backend.dto.vehicle;

import com.uberplus.backend.model.enums.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleMapDTO {
    private Integer id;
    private Double lat;
    private Double lng;
    private VehicleStatus status;
}
