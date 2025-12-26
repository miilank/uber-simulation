package com.uberplus.backend.dto.driver;

import com.uberplus.backend.dto.vehicle.VehicleCreationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverCreationDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private VehicleCreationDTO vehicle;
}
