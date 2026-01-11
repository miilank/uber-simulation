package com.uberplus.backend.config;

import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Location;
import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class VehicleMapDevSeeder implements CommandLineRunner {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${uberplus.seed.vehicles:100}")
    private int vehicleCount;

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0 || driverRepository.count() > 0) return;

        Random rnd = new Random();
        VehicleType defaultType = VehicleType.values()[0];

        for (int i = 1; i <= vehicleCount; i++) {
            Driver d = new Driver();

            d.setAddress("Novi Sad");
            d.setFirstName("Driver");
            d.setLastName(String.valueOf(i));
            d.setEmail("driver" + i + "@uberplus.com");
            d.setPhoneNumber("+381600000" + i);
            d.setPassword("test123");
            d.setRole(UserRole.DRIVER);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            d.setActivated(true);
            d.setBlocked(false);

            d.setAvailable(i % 2 == 0);
            d.setActive(true);
            d.setWorkedMinutesLast24h(0.0);
            d.setAverageRating(null);

            d = driverRepository.save(d);

            double lat = 45.2671 + (rnd.nextDouble() - 0.5) * 0.08;
            double lng = 19.8335 + (rnd.nextDouble() - 0.5) * 0.08;

            Location loc = new Location(
                    null,
                    lat,
                    lng,
                    "Novi Sad",
                    LocalDateTime.now()
            );

            Vehicle v = new Vehicle();
            v.setModel("Model " + i);
            v.setType(defaultType);
            v.setLicensePlate("NS-" + (1000 + i));
            v.setSeatCount(4);
            v.setBabyFriendly(i % 2 == 0);
            v.setPetsFriendly(i % 5 == 0);
            v.setStatus((i % 3 == 0) ? VehicleStatus.OCCUPIED : VehicleStatus.AVAILABLE);

            v.setCurrentLocation(loc);
            v.setDriver(d);
            d.setVehicle(v);

            vehicleRepository.save(v);
        }
    }
}
