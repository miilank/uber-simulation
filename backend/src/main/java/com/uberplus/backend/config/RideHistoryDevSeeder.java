package com.uberplus.backend.config;

import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.LocationRepository;
import com.uberplus.backend.repository.PassengerRepository;
import com.uberplus.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class RideHistoryDevSeeder implements CommandLineRunner {

    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final LocationRepository locationRepository;
    private final RideRepository rideRepository;

    @Override
    public void run(String... args) {
        if (rideRepository.count() > 0) return;
        if (driverRepository.count() == 0) return;

        Random rnd = new Random();

        if (passengerRepository.count() == 0) {
            for (int i = 1; i <= 10; i++) {
                Passenger p = new Passenger();
                p.setEmail("passenger" + i + "@uberplus.com");
                p.setPassword("test123");
                p.setFirstName("Passenger");
                p.setLastName(String.valueOf(i));
                p.setAddress("Novi Sad");
                p.setPhoneNumber("+381650000" + i);
                p.setRole(UserRole.PASSENGER);
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                p.setActivated(true);
                passengerRepository.save(p);
            }
        }

        List<Passenger> passengers = passengerRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();

        for (Driver d : drivers.stream().limit(8).toList()) {
            for (int j = 0; j < 8; j++) {

                Passenger creator = passengers.get(rnd.nextInt(passengers.size()));

                Location start = locationRepository.save(new Location(
                        null,
                        45.2671 + (rnd.nextDouble() - 0.5) * 0.06,
                        19.8335 + (rnd.nextDouble() - 0.5) * 0.06,
                        "Bulevar oslobođenja " + (10 + rnd.nextInt(200)),
                        LocalDateTime.now()
                ));

                Location end = locationRepository.save(new Location(
                        null,
                        45.2671 + (rnd.nextDouble() - 0.5) * 0.06,
                        19.8335 + (rnd.nextDouble() - 0.5) * 0.06,
                        "Naučno tehnološki park",
                        LocalDateTime.now()
                ));

                Ride r = new Ride();
                r.setCreator(creator);
                r.setDriver(d);

                RideStatus st = (j % 3 == 0) ? RideStatus.CANCELLED : RideStatus.COMPLETED;
                r.setStatus(st);

                r.setStartLocation(start);
                r.setEndLocation(end);

                r.setVehicleType(VehicleType.values()[0]);
                r.setBabyFriendly(rnd.nextBoolean());
                r.setPetsFriendly(rnd.nextInt(5) == 0);

                LocalDateTime created = LocalDateTime.now()
                        .minusDays(rnd.nextInt(60))
                        .minusHours(rnd.nextInt(24));

                r.setCreatedAt(created);

                LocalDateTime startTime = created.plusMinutes(5 + rnd.nextInt(20));
                LocalDateTime endTime = startTime.plusMinutes(10 + rnd.nextInt(25));

                r.setEstimatedStartTime(startTime);
                r.setEstimatedEndTime(endTime);
                r.setActualStartTime(startTime);
                r.setActualEndTime(endTime);

                r.setDistanceKm(2.0 + rnd.nextDouble() * 10.0);
                r.setBasePrice(3.0);
                r.setTotalPrice(5.0 + rnd.nextDouble() * 20.0);

                boolean panic = (rnd.nextInt(10) < 2);
                r.setPanicActivated(panic);
                if (panic) {
                    r.setPanicActivatedAt(startTime.plusMinutes(3));
                    r.setPanicActivatedBy("Driver");
                }

                if (st == RideStatus.CANCELLED) {
                    r.setCancelledBy(rnd.nextBoolean() ? "User" : "Driver");
                    r.setCancellationReason("Changed plans");
                    r.setCancellationTime(startTime.minusMinutes(1));
                }

                r.getPassengers().add(creator);

                rideRepository.save(r);
            }
        }
    }
}
