package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;
import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.repository.VehicleRepository;
import com.uberplus.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public List<VehicleMapDTO> getVehiclesForMap() {
        List<Vehicle> vehicles = vehicleRepository.findByStatusIn(
                List.of(VehicleStatus.AVAILABLE, VehicleStatus.OCCUPIED)
        );

        return vehicles.stream()
                .filter(v -> v.getCurrentLocation() != null)
                .map(v -> new VehicleMapDTO(
                        v.getId(),
                        v.getCurrentLocation().getLatitude(),
                        v.getCurrentLocation().getLongitude(),
                        v.getStatus()
                ))
                .toList();
    }
}
