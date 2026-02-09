package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.passenger.PassengerDTO;
import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.dto.ride.RideDetailDTO;
import com.uberplus.backend.dto.ride.RideHistoryItemDTO;
import com.uberplus.backend.dto.ride.RideInconsistencyDTO;
import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.RideInconsistency;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.repository.RideInconsistencyRepository;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.service.RideHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideHistoryServiceImpl implements RideHistoryService {

    private final RideRepository rideRepository;
    private final RideInconsistencyRepository rideInconsistencyRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.");
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

        return new RideHistoryItemDTO(
                r.getId(),
                date,
                time,
                r.getStartLocation() != null ? r.getStartLocation().getAddress() : "",
                r.getEndLocation() != null ? r.getEndLocation().getAddress() : "",
                r.getStatus() != null ? r.getStatus().name() : "PENDING",
                r.getCancelledBy(),
                r.isPanicActivated(),
                price
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
}
