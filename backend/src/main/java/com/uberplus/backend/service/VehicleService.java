package com.uberplus.backend.service;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;

import java.util.List;

public interface VehicleService {
    List<VehicleMapDTO> getVehiclesForMap();
}
