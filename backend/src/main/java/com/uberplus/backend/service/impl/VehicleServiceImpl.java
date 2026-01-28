package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;
import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.repository.VehicleRepository;
import com.uberplus.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.uberplus.backend.dto.vehicle.VehiclePositionUpdateDTO;
import com.uberplus.backend.model.Location;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    @Override
    public VehicleMapDTO getDriverVehicleForMap(String driverEmail) {
        Vehicle v = vehicleRepository.findByDriverEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Vehicle not found for driver."));

        if (v.getCurrentLocation() == null) {
            return new VehicleMapDTO(v.getId(), 0.0, 0.0, v.getStatus());
        }

        return new VehicleMapDTO(
                v.getId(),
                v.getCurrentLocation().getLatitude(),
                v.getCurrentLocation().getLongitude(),
                v.getStatus()
        );
    }

    @Override
    @Transactional
    public void updateVehiclePosition(Integer vehicleId, VehiclePositionUpdateDTO dto) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found."));

        if (v.getCurrentLocation() == null) {
            Location loc = new Location();
            loc.setLatitude(dto.getLatitude());
            loc.setLongitude(dto.getLongitude());
            v.setCurrentLocation(loc);
        } else {
            v.getCurrentLocation().setLatitude(dto.getLatitude());
            v.getCurrentLocation().setLongitude(dto.getLongitude());
        }

        vehicleRepository.save(v);
    }

    @Override
    @Transactional
    public void updateVehicleStatus(Integer vehicleId, VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicle not found"
                ));

        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);
    }
}
