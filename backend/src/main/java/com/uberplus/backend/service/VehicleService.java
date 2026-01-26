package com.uberplus.backend.service;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;
import com.uberplus.backend.dto.vehicle.VehiclePositionUpdateDTO;
import com.uberplus.backend.model.enums.VehicleStatus;

import java.util.List;

public interface VehicleService {
    List<VehicleMapDTO> getVehiclesForMap();
    VehicleMapDTO getDriverVehicleForMap(String driverEmail);
    void updateVehiclePosition(Integer vehicleId, VehiclePositionUpdateDTO dto);
    void updateVehicleStatus(Integer vehicleId, VehicleStatus status);
}
