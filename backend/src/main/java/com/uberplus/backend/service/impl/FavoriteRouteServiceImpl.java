package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.dto.route.FavoriteRouteCreateDTO;
import com.uberplus.backend.dto.route.FavoriteRouteDTO;
import com.uberplus.backend.model.FavoriteRoute;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.User;
import com.uberplus.backend.repository.FavoriteRouteRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.FavoriteRouteService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class FavoriteRouteServiceImpl implements FavoriteRouteService {
    private final FavoriteRouteRepository routeRepository;
    private final UserRepository userRepository;

    @Override
    public List<FavoriteRouteDTO> getFavRoutes(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        return routeRepository.findByUser(user).stream().map(FavoriteRouteDTO::new).toList();
    }

    @Override
    public FavoriteRouteDTO createFavRoute(FavoriteRouteCreateDTO request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        List<FavoriteRoute> oldRoutes = routeRepository.findByNameAndUser(request.getName(), user);

        if(!oldRoutes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Route with this name already exists.");
        }

        FavoriteRoute route = new FavoriteRoute();
        route.setName(request.getName());

        route.setStartLocation(request.getStartLocation().toEntity());
        route.setEndLocation(request.getEndLocation().toEntity());

        if (request.getWaypoints() != null) {
            route.setWaypoints(
                    request.getWaypoints()
                            .stream()
                            .map(LocationDTO::toEntity)
                            .toList()
            );
        }

        route.setVehicleType(request.getVehicleType());
        route.setBabyFriendly(request.isBabyFriendly());
        route.setPetsFriendly(request.isPetsFriendly());
        route.setUser((Passenger) user);

        route.setCreatedAt(LocalDateTime.now());

        FavoriteRoute savedRoute = routeRepository.save(route);

        return new FavoriteRouteDTO(savedRoute);
    }

    @Override
    public FavoriteRouteDTO getRoute(Integer routeId) {
        FavoriteRoute route = routeRepository.findById(routeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        return new FavoriteRouteDTO(route);
    }

    @Override
    @Transactional
    public void deleteFavRoute(Integer routeId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        FavoriteRoute route = routeRepository.findById(routeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found."));

        if (route.getUser() == null || !route.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this route.");
        }

        routeRepository.delete(route);
    }
}
