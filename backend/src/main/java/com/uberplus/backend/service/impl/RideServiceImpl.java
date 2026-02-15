package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.notification.PanicNotificationDTO;
import com.uberplus.backend.dto.ride.*;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.repository.*;
import com.uberplus.backend.service.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.partitioningBy;

@Service
@AllArgsConstructor
public class RideServiceImpl implements RideService {
    private RideRepository rideRepository;
    private UserRepository userRepository;
    private DriverRepository driverRepository;
    private PassengerRepository passengerRepository;
    private OSRMService osrmService;
    private RideInconsistencyRepository rideInconsistencyRepository;
    private final EmailService emailService;
    private PricingService pricingService;
    private NotificationService notificationService;

    @Override
    @Transactional
    public RideDTO requestRide(String email, CreateRideRequestDTO request) {
        if (request.getLinkedPassengerEmails() == null) {
            request.setLinkedPassengerEmails(new ArrayList<>());
        }

        if(request.getWaypoints() == null) {
            request.setWaypoints(new ArrayList<>());
        }

        Passenger creator = (Passenger) userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        if (creator.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.LOCKED, creator.getBlockReason());
        }

        List<Driver> potentialDrivers = driverRepository.findByActiveTrueAndBlockedFalse();

        if (potentialDrivers.isEmpty()) throw new ResponseStatusException(HttpStatus.OK, "No drivers currently active.");

        LocalDateTime scheduledEnd = request.getScheduledTime().plusMinutes(request.getEstimatedDurationMinutes());

        // Filter drivers that will be inactive
        potentialDrivers = potentialDrivers.stream().filter(driver -> {
           LocalDateTime workEndTime = LocalDateTime.now().plusMinutes((long)(8*60-driver.getWorkedMinutesLast24h()));
           return scheduledEnd.isBefore(workEndTime);
        }).toList();

        if(potentialDrivers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No drivers will be active during your scheduled time.");
        }

        //Filter drivers with overlapping rides
        potentialDrivers =  potentialDrivers.stream().filter(driver -> {
            List<Ride> rides = driver.getRides();
            for(Ride ride : rides) {
                if ((ride.getEstimatedStartTime().isBefore(scheduledEnd) &&
                        ride.getEstimatedEndTime().isAfter(request.getScheduledTime()))) {
                    return false;
                }
            }
            return true;
        }).toList();

        if(potentialDrivers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "All drivers are busy during your scheduled time.");
        }

        // Filter drivers by vehicle requirements
        potentialDrivers = potentialDrivers.stream().filter(driver -> {
            Vehicle vehicle = driver.getVehicle();
            return ((!request.isBabyFriendly() || vehicle.isBabyFriendly()) &&
                    (!request.isPetFriendly() || vehicle.isPetsFriendly()) &&
                    ((request.getVehicleType() == vehicle.getType()) || (request.getVehicleType()==null)) &&
                    (vehicle.getSeatCount() >= (request.getLinkedPassengerEmails().size() + 1)));
        }).toList();

        if(potentialDrivers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No available drivers meet your requirements.");
        }


        List<Driver> availableDrivers = filterDriversWhoCanMakeRide(potentialDrivers,
                scheduledEnd,
                request.getEndLocation().toEntity());

        if(availableDrivers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK,
                    "Cannot assign this ride because the time is incompatible with drivers' schedules (debug message).");
        }

        // Finally, sort drivers based on how far they will be after their last ride before the scheduled ride
        // If there was no previous ride, take driver's location
        Location requestStart = request.getStartLocation().toEntity();

        Driver selectedDriver = availableDrivers.stream()
                .map(driver -> {
                    Ride lastRide = getLastRideBefore(request.getScheduledTime(), driver);
                    long distance;
                    if (lastRide != null && lastRide.getEndLocation() != null) {
                        distance = (long) lastRide.getEndLocation().distanceTo(requestStart);

                    } else if (driver.getVehicle().getCurrentLocation() != null) {
                        distance = (long) driver.getVehicle().getCurrentLocation().distanceTo(requestStart);

                    } else {
                        distance = Long.MAX_VALUE;
                    }
                    return new AbstractMap.SimpleEntry<>(driver, distance);
                })
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toList()
                .getFirst();


        Ride ride = new Ride();
        ride.setCreator(creator);
        ride.setDriver(selectedDriver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setStartLocation(request.getStartLocation().toEntity());
        ride.setEndLocation(request.getEndLocation().toEntity());

        ride.setDistanceKm(request.getDistanceKm());
        ride.setBasePrice(pricingService.calculatePrice(request.getDistanceKm(),
                                                        selectedDriver.getVehicle().getType()));
        ride.setTotalPrice(ride.getBasePrice());
        List<Location> waypoints = new ArrayList<>();

        for(LocationDTO dto : request.getWaypoints()) {
            waypoints.add(dto.toEntity());
        }
        ride.setWaypoints(waypoints);

        // Estimate arrival time
        Ride lastRide = getLastRideBefore(request.getScheduledTime(), selectedDriver);
        Location loc;
        LocalDateTime vehicleStartTime; // Time at which vehicle starts moving to pickup
        if (lastRide != null) {
            loc = lastRide.getEndLocation();
            vehicleStartTime = lastRide.getEstimatedEndTime();
        } else {
            loc = selectedDriver.getVehicle().getCurrentLocation();
            vehicleStartTime = LocalDateTime.now();
        }

        LocalDateTime estArrival;
        try {
            estArrival = vehicleStartTime
                    .plusSeconds((long) osrmService.getDuration(loc, requestStart));
        } catch (IOException | InterruptedException e) {
            // If error, assume ride will be on time
            estArrival = request.getScheduledTime();
        }

        if(estArrival.isBefore(request.getScheduledTime())) {
            ride.setEstimatedStartTime(request.getScheduledTime());
        } else {
            ride.setEstimatedStartTime(estArrival);
        }

        ride.setEstimatedEndTime(ride.getEstimatedStartTime()
                .plusMinutes(request.getEstimatedDurationMinutes()));

        ride.setVehicleType(selectedDriver.getVehicle().getType());
        ride.setBabyFriendly(request.isBabyFriendly());
        ride.setPetsFriendly(request.isPetFriendly());

        ride.getPassengers().add(creator);

        if (request.getLinkedPassengerEmails() != null) {
            for (String pEmail : request.getLinkedPassengerEmails()) {
                if (pEmail == null) continue;

                String trimmed = pEmail.trim();
                if (trimmed.isEmpty()) continue;

//                if (trimmed.equalsIgnoreCase(passenger.getEmail())) continue;

                Passenger linked = (Passenger) userRepository.findByEmail(trimmed)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Linked passenger not found: " + trimmed
                        ));

                boolean alreadyAdded = ride.getPassengers().stream()
                        .anyMatch(pp -> pp.getId().equals(linked.getId()));
                if (!alreadyAdded) {
                    ride.getPassengers().add(linked);
                }
            }
        }

        ride.setScheduledTime(request.getScheduledTime());
        ride.setCreatedAt(LocalDateTime.now());
        rideRepository.save(ride);
        notificationService.notifyRideAccepted(ride);
        return new RideDTO(ride);
    }

    @Override
    public List<RideDTO> getRides(String email) {
        Driver user = driverRepository.findByEmail(email).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        return rideRepository.findByDriver((Driver) user)
                .stream()
                .map(RideDTO::new)
                .filter(rideDTO -> (rideDTO.getStatus() != RideStatus.CANCELLED && rideDTO.getStatus() != RideStatus.COMPLETED && rideDTO.getStatus() != RideStatus.STOPPED))
                .toList();
    }
    @Override
    public List<RideDTO> getPassengerRides(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        return rideRepository.findActiveRidesByPassengerEmail(email)
                .stream()
                .map(RideDTO::new)
                .toList();
    }
    @Override
    @Transactional
    public RideDTO startRide(Integer rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found.")
        );

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride must be ACCEPTED to start.");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setActualStartTime(LocalDateTime.now());

        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            ride.getDriver().getVehicle().setStatus(VehicleStatus.OCCUPIED);
        }

        rideRepository.save(ride);
        return new RideDTO(ride);
    }

    @Override
    public void setPanic(Integer rideId, Integer userId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found.")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (ride.isPanicActivated()){
            return;
        }
        ride.setPanicActivatedAt(LocalDateTime.now());
        ride.setPanicActivatedBy(user.getEmail());
        ride.setPanicActivated(true);
        rideRepository.save(ride);
    }
    @Override
    public List<PanicNotificationDTO> getPanicNotifications(){
        List<Ride> ridesWithPanic = rideRepository.findAllByPanicActivated(true);
        return ridesWithPanic.stream()
                .map(ride -> {
                    PanicNotificationDTO dto = new PanicNotificationDTO();
                    dto.setRideId(ride.getId());
                    dto.setActivatedBy(ride.getPanicActivatedBy());
                    dto.setDriverId(ride.getDriver().getId());
                    dto.setTimestamp(ride.getPanicActivatedAt());
                    dto.setUserType(userRepository.findByEmail(ride.getPanicActivatedBy()).get().getRole());
                    return dto;
                })
                .collect(Collectors.toList());

    }
    @Override
    public void resolvePanic(Integer rideId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found.")
        );
        ride.setPanicActivated(false);
        ride.setPanicActivatedAt(null);
        ride.setPanicActivatedBy(null);
        rideRepository.save(ride);
    }

    @Override
    public RideDTO getInProgressForPassenger(String email) {
        Ride ride = rideRepository.findInProgressForPassenger(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No IN_PROGRESS ride."));
        return new RideDTO(ride);
    }

    private Ride getLastRideBefore(LocalDateTime date, Driver driver) {
        // Filter canceled/completed rides and those after set date
        List<Ride> rides = driver.getRides().stream().filter(ride ->
                ride.getEstimatedEndTime().isBefore(date) &&
                (ride.getStatus() == RideStatus.PENDING || ride.getStatus() == RideStatus.IN_PROGRESS)).toList();

        rides = new ArrayList<>(rides);
        rides.sort(Comparator.comparing(Ride::getEstimatedEndTime));

        return rides.isEmpty() ? null : rides.getLast();
    }

    private Ride getFirstRideAfter(LocalDateTime date, Driver driver) {
        // Filter canceled/completed rides and those before set date
        List<Ride> rides = driver.getRides().stream().filter(ride ->
                ride.getEstimatedStartTime().isAfter(date) &&
                (ride.getStatus() == RideStatus.PENDING || ride.getStatus() == RideStatus.IN_PROGRESS)).toList();

        rides = new ArrayList<>(rides);
        rides.sort(Comparator.comparing(Ride::getEstimatedEndTime));

        return rides.isEmpty() ? null : rides.getFirst();
    }


    private List<Driver> filterDriversWhoCanMakeRide(List<Driver> potentialDrivers,
                                                     LocalDateTime scheduledEnd,
                                                     Location startLocation) {
        Map<Boolean, List<Map.Entry<Driver, Ride>>> pairedParts =
                potentialDrivers.stream()
                        .map(driver -> new AbstractMap.SimpleEntry<>(driver, getFirstRideAfter(scheduledEnd, driver)))
                        .collect(Collectors.partitioningBy( entry -> entry.getValue() != null));

        // Split list based on if they have another ride afterwards or not
        List<Driver> hasNextDrivers = pairedParts.get(true).stream()
                .map(Map.Entry::getKey)
                .toList();

        List<Ride> nextRides = pairedParts.get(true).stream()
                .map(Map.Entry::getValue)
                .toList();

        List<Location> nextLocations = nextRides.stream()
                .map(Ride::getStartLocation)
                .toList();

        List<Driver> noNextDrivers = pairedParts.get(false).stream()
                .map(Map.Entry::getKey)
                .toList();

        double[][] durationMatrix;
        try {
            durationMatrix = osrmService.getDurationsMatrix(nextLocations,
                    new ArrayList<>(Collections.singletonList(startLocation)));
        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Routing service temporarily unavailable.");
        }

        // Goes through drivers with next rides, then appends those who can get to the ride before its scheduled start
        ArrayList<Driver> availableDrivers = new ArrayList<>(noNextDrivers);
        for (int i = 0; i < hasNextDrivers.size(); i++) {
            double durationSeconds = durationMatrix[i][0];
            if (scheduledEnd.plusSeconds((long)durationSeconds)
                    .isBefore(nextRides.get(i).getScheduledTime())) {
                availableDrivers.add(hasNextDrivers.get(i));
            }
        }

        return availableDrivers;
    }

    @Override
    @Transactional
    public RideDTO completeRide(Integer rideId, String driverEmail) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found."
                ));

        if (ride.getDriver() == null || !ride.getDriver().getEmail().equals(driverEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not the driver of this ride."
            );
        }

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ride is not in progress."
            );
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setActualEndTime(LocalDateTime.now());

        Driver driver = ride.getDriver();
        if (driver.getVehicle() != null) {
            driver.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        }

        if (ride.getActualStartTime() != null) {
            long minutesWorked = java.time.Duration.between(
                    ride.getActualStartTime(),
                    ride.getActualEndTime()
            ).toMinutes();

            driver.setWorkedMinutesLast24h(
                    driver.getWorkedMinutesLast24h() + minutesWorked
            );
        }

        rideRepository.save(ride);
        driverRepository.save(driver);
        notificationService.notifyRideCompleted(ride);
        return new RideDTO(ride);
    }

    @Override
    public RideDTO cancelRide(Integer rideId, String reason, Integer userId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found."
                ));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found."
                ));
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(reason);
        ride.setCancellationTime(LocalDateTime.now());
        ride.setCancelledBy(user.getEmail());
        rideRepository.save(ride);

        return new RideDTO(ride);
    }

    @Override
    public RideETADTO getRideETA(Integer rideId) throws IOException, InterruptedException {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found."));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is not in progress.");
        }

        Vehicle vehicle = ride.getDriver().getVehicle();
        Location currentLocation = vehicle.getCurrentLocation();

        if (currentLocation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle location unknown.");
        }

        RideETADTO eta = new RideETADTO();
        eta.setRideId(rideId);

        Location pickupLocation = ride.getStartLocation();
        Location destinationLocation = ride.getEndLocation();

        double distanceToPickup = currentLocation.distanceTo(pickupLocation);
        double distanceToDestination = currentLocation.distanceTo(destinationLocation);

        if (distanceToDestination < 50) {
            eta.setPhase("IN_PROGRESS");
            eta.setDistanceToNextPointKm(0.0);
            eta.setEtaToNextPointSeconds(0);
            eta.setProgressPercent(100.0);
            return eta;
        }

        if (ride.getArrivedAtPickupTime() != null || distanceToPickup < 50) {
            double durationSeconds = osrmService.getDuration(currentLocation, destinationLocation);

            eta.setPhase("IN_PROGRESS");
            eta.setDistanceToNextPointKm(distanceToDestination / 1000.0);
            eta.setEtaToNextPointSeconds((int) durationSeconds);

            double totalDistance = pickupLocation.distanceTo(destinationLocation);
            double remainingDistance = distanceToDestination;
            double traveled = totalDistance - remainingDistance;
            double progress = totalDistance > 0 ? (traveled / totalDistance) * 100.0 : 0.0;
            eta.setProgressPercent(Math.max(0, Math.min(100, progress)));

        } else {
            double durationSeconds = osrmService.getDuration(currentLocation, pickupLocation);

            eta.setPhase("TO_PICKUP");
            eta.setDistanceToNextPointKm(distanceToPickup / 1000.0);
            eta.setEtaToNextPointSeconds((int) durationSeconds);
            eta.setProgressPercent(0.0);
        }

        return eta;
    }

    @Override
    @Transactional
    public RideDTO arrivedAtPickup(Integer rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found."));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride must be IN_PROGRESS.");
        }

        ride.setArrivedAtPickupTime(LocalDateTime.now());

        rideRepository.save(ride);
        return new RideDTO(ride);
    }

    @Override
    @Transactional
    public void reportInconsistency(Integer rideId, String passengerEmail, String description) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found."
                ));

        Passenger passenger = passengerRepository.findByUserEmail(passengerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passenger not found."
                ));

        boolean isPassengerInRide = ride.getPassengers().stream()
                .anyMatch(p -> p.getId().equals(passenger.getId()));

        if (!isPassengerInRide) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not a passenger in this ride."
            );
        }

        RideInconsistency inconsistency = new RideInconsistency();
        inconsistency.setRide(ride);
        inconsistency.setReportedBy(passenger);
        inconsistency.setDescription(description);
        inconsistency.setCreatedAt(LocalDateTime.now());

        rideInconsistencyRepository.save(inconsistency);
    }

    @Override
    @Transactional
    public RideDTO stopEarly(Integer rideId, LocationDTO dto){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found."));

        Location endLocation = new Location();
        endLocation.setLongitude(dto.getLongitude());
        endLocation.setLatitude(dto.getLatitude());
        endLocation.setAddress(dto.getAddress());

        ride.setStoppedAt(LocalDateTime.now());
        ride.setActualEndTime(LocalDateTime.now());
        ride.setEndLocation(endLocation);
        ride.setStoppedLocation(endLocation);
        ride.setStatus(RideStatus.STOPPED);
        double totalDistance = endLocation.distanceTo(ride.getStartLocation());
        double actualPrice = pricingService.calculatePrice((int)(totalDistance/1000),ride.getVehicleType());
        ride.setTotalPrice(actualPrice);

        Driver driver = ride.getDriver();
        if (driver.getVehicle() != null) {
            driver.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        }

        if (ride.getActualStartTime() != null) {
            long minutesWorked = java.time.Duration.between(
                    ride.getActualStartTime(),
                    ride.getActualEndTime()
            ).toMinutes();

            driver.setWorkedMinutesLast24h(
                    driver.getWorkedMinutesLast24h() + minutesWorked
            );
        }
        driverRepository.save(driver);
        rideRepository.save(ride);
        return new RideDTO(ride);
    }
}


