package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.passenger.PassengerDTO;
import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.dto.ride.*;
import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.RideInconsistency;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.RatingRepository;
import com.uberplus.backend.repository.RideInconsistencyRepository;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.RideHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RideHistoryServiceImpl implements RideHistoryService {

    private final RideRepository rideRepository;
    private final RideInconsistencyRepository rideInconsistencyRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public RideHistoryResponseDTO getDriverHistory(Integer driverId, RideHistoryFilterDTO filter) {
        int page = (filter.getPage() == null || filter.getPage() < 0) ? 0 : filter.getPage();
        int size = (filter.getSize() == null || filter.getSize() <= 0) ? 20 : filter.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime from;
        LocalDateTime to;

        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();
        if (start != null) from = start.atStartOfDay();
        else {
            from = null;
        }
        if (end != null) to = end.plusDays(1).atStartOfDay();
        else {
            to = null;
        }

        List<RideStatus> statuses = List.of(
                RideStatus.COMPLETED,
                RideStatus.CANCELLED,
                RideStatus.STOPPED
        );

        Specification<Ride> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("driver").get("id"), driverId);
            p = cb.and(p, root.get("status").in(statuses));
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) p = cb.and(p, cb.lessThan(root.get("createdAt"), to));
            return p;
        };

        var pageRes = rideRepository.findAll(spec, pageable);
        var items = pageRes.getContent().stream().map(this::toDto).toList();

        return new RideHistoryResponseDTO(items, pageRes.getTotalElements(), pageRes.getNumber(), pageRes.getSize());
    }
    @Override
    public RideHistoryResponseDTO getPassengerHistory(Integer userId, RideHistoryFilterDTO filter) {
        int page = (filter.getPage() == null || filter.getPage() < 0) ? 0 : filter.getPage();
        int size = (filter.getSize() == null || filter.getSize() <= 0) ? 20 : filter.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime from;
        LocalDateTime to;

        LocalDate start = filter.getStartDate();
        LocalDate end = filter.getEndDate();
        if (start != null) from = start.atStartOfDay();
        else {
            from = null;
        }
        if (end != null) to = end.plusDays(1).atStartOfDay();
        else {
            to = null;
        }

        List<RideStatus> statuses = List.of(
                RideStatus.COMPLETED,
                RideStatus.CANCELLED,
                RideStatus.STOPPED
        );

        Specification<Ride> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("creator").get("id"), userId);
            p = cb.and(p, root.get("status").in(statuses));
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) p = cb.and(p, cb.lessThan(root.get("createdAt"), to));
            return p;
        };

        var pageRes = rideRepository.findAll(spec, pageable);
        var items = pageRes.getContent().stream().map(this::toDto).toList();

        return new RideHistoryResponseDTO(items, pageRes.getTotalElements(), pageRes.getNumber(), pageRes.getSize());
    }
    private RideHistoryItemDTO toDto(Ride r) {
        LocalDateTime start = (r.getActualStartTime() != null) ? r.getActualStartTime() : r.getEstimatedStartTime();
        LocalDateTime end = (r.getActualEndTime() != null) ? r.getActualEndTime() : r.getEstimatedEndTime();

        String date = (r.getCreatedAt() != null) ? r.getCreatedAt().format(DATE_FMT) : "";

        String time = "";
        if (start != null && end != null) {
            time = start.format(TIME_FMT) + " - " + end.format(TIME_FMT);
        } else if (start != null) {
            time = start.format(TIME_FMT);
        }

        String price = (r.getTotalPrice() != null) ? String.format("€%.2f", r.getTotalPrice()) : "€0.00";

        boolean alreadyRated = ratingRepository.existsByRideIdAndPassengerId(
                r.getId(),
                r.getCreator().getId()
        );

        return new RideHistoryItemDTO(
                r.getId(),
                date,
                time,
                r.getStartLocation() != null ? r.getStartLocation().getAddress() : "",
                r.getEndLocation() != null ? r.getEndLocation().getAddress() : "",
                r.getStatus() != null ? r.getStatus().name() : "PENDING",
                r.getCancelledBy(),
                r.isPanicActivated(),
                price,
                r.getActualEndTime(),
                alreadyRated
        );
    }

    @Override
    public RideDetailDTO getRideDetails(Integer rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        List<LocationDTO> waypoints = ride.getWaypoints().stream()
                .map(LocationDTO::new)
                .toList();

        List<PassengerDTO> passengers = ride.getPassengers().stream()
                .map(PassengerDTO::new)
                .toList();
        DriverDTO driver = new DriverDTO(ride.getDriver(),ride.getDriver().getProfilePicture());
        List<RideInconsistency> inconsistencies = rideInconsistencyRepository
                .findByRideId(rideId);

        List<RideInconsistencyDTO> inconsistencyDTOs = inconsistencies.stream()
                .map(inc -> new RideInconsistencyDTO(
                        inc.getRide().getId(),
                        inc.getReportedBy().getId(),
                        inc.getReportedBy().getFirstName() + " " + inc.getReportedBy().getLastName(),
                        inc.getDescription(),
                        inc.getCreatedAt()
                ))
                .toList();

        return new RideDetailDTO(
                ride.getId(),
                ride.getStatus().name(),
                ride.getStartLocation().getAddress(),
                ride.getEndLocation().getAddress(),
                ride.getActualStartTime(),
                ride.getActualEndTime(),
                ride.getEstimatedStartTime(),
                ride.getEstimatedEndTime(),
                waypoints,
                passengers,
                driver,
                ride.getTotalPrice(),
                ride.getCancelledBy(),
                ride.getCancellationReason(),
                ride.getCancellationTime(),
                ride.isPanicActivated(),
                ride.getPanicActivatedBy(),
                ride.getPanicActivatedAt(),
                ride.getStoppedLocation() != null ? ride.getStoppedLocation().getAddress() : null,
                ride.getStoppedAt(),
                inconsistencyDTOs
        );
    }

    @Override
    public HistoryReportDTO getRideHistoryReport(String name, LocalDate from, LocalDate to, Integer uuid) {
        User subject;
        User user = userRepository.findByEmail(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));;
        UserRole role;

        if (uuid == null) {
            subject = user;
            role = user.getRole();
        } else {
            if(user.getRole() != UserRole.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have the authorization for this activity.");
            }
            subject = userRepository.findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
            role = subject.getRole();
        }

        Specification<Ride> spec = (root, query, cb) -> {
            Predicate p;
            if (role == UserRole.DRIVER) {
                p = cb.equal(root.get("driver").get("id"), subject.getId());
            } else if (role == UserRole.PASSENGER) {
                p = cb.equal(root.get("creator").get("id"), subject.getId());
            } else if (role == UserRole.ADMIN) {
                p = cb.conjunction();
            } else {
                p = cb.disjunction();
            }
            p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return p;
        };

        List<Ride> rides = rideRepository.findAll(spec);

        Map<LocalDate, HistoryReportDTO.RowElementDTO> map = new TreeMap<>();

        for(Ride ride : rides) {
            LocalDate date = LocalDate.from(ride.getActualStartTime() == null? ride.getEstimatedStartTime() : ride.getActualStartTime());
            map.merge(date, createRow(date, ride), (existing, newRow) -> {
                existing.setNumberOfRides(existing.getNumberOfRides() + 1);
                existing.setKms(existing.getKms() + getDoubleOrZero(ride.getDistanceKm()));
                existing.setMoney(existing.getMoney() + getDoubleOrZero(ride.getTotalPrice()));
                return existing;
            });
        }

        List<HistoryReportDTO.RowElementDTO> rows = new ArrayList<>(map.values());

        ArrayList<Double> cumulativeMoney = new ArrayList<>(rows.size());
        ArrayList<Double> cumulativeKms = new ArrayList<>(rows.size());
        ArrayList<Integer> cumulativeRides = new ArrayList<>(rows.size());

        double runningMoney = 0.0;
        double runningKms = 0.0;
        int runningRides = 0;

        double totalMoney = 0.0;
        double totalKms = 0.0;
        int totalRides = 0;

        for (HistoryReportDTO.RowElementDTO r : rows) {
            totalMoney += r.getMoney();
            totalKms += r.getKms();
            totalRides += r.getNumberOfRides();

            runningMoney += r.getMoney();
            runningKms += r.getKms();
            runningRides += r.getNumberOfRides();

            cumulativeMoney.add(runningMoney);
            cumulativeKms.add(runningKms);
            cumulativeRides.add(runningRides);
        }


        HistoryReportDTO dto = new HistoryReportDTO();
        dto.setRows(rows);
        dto.setCumulativeMoney(cumulativeMoney);
        dto.setCumulativeKms(cumulativeKms);
        dto.setCumulativeRides(cumulativeRides);
        dto.setTotalMoney(totalMoney);
        dto.setTotalKms(totalKms);
        dto.setTotalRides(totalRides);

        return dto;
    }

    private HistoryReportDTO.RowElementDTO createRow(LocalDate date, Ride ride) {
        HistoryReportDTO.RowElementDTO row = new HistoryReportDTO.RowElementDTO();
        row.setDate(date);
        row.setNumberOfRides(1);
        row.setKms(getDoubleOrZero(ride.getDistanceKm()));
        row.setMoney(getDoubleOrZero(ride.getTotalPrice()));
        return row;
    }

    private double getDoubleOrZero(Double value) {
        return value != null ? value : 0.0;
    }
}
