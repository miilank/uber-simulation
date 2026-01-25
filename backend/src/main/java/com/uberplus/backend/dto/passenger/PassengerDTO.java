package com.uberplus.backend.dto.passenger;

import com.uberplus.backend.model.Passenger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {
    private String firstName;
    private String lastName;
    private String email;

    public PassengerDTO(Passenger p) {
        this.firstName = p.getFirstName();
        this.lastName = p.getLastName();
        this.email = p.getEmail();
    }
}
