package com.uberplus.backend.dto.driver;

import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.ProfileChangeRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverUpdateDTO {
    private Integer driverId;

    private String email;

    private String newFirstName;
    private String newLastName;
    private String newPhoneNumber;
    private String newAddress;
    private String newProfilePicture;

    private String oldFirstName;
    private String oldLastName;
    private String oldPhoneNumber;
    private String oldAddress;
    private String oldProfilePicture;

    public DriverUpdateDTO(ProfileChangeRequest request) {
        Driver driver = request.getDriver();

        driverId = driver.getId();
        email = driver.getEmail();

        newFirstName = request.getFirstName();
        newLastName = request.getLastName();
        newPhoneNumber = request.getPhoneNumber();
        newAddress = request.getAddress();

        oldFirstName = driver.getFirstName();
        oldLastName = driver.getLastName();
        oldPhoneNumber = driver.getPhoneNumber();
        oldAddress = driver.getAddress();
    }
}