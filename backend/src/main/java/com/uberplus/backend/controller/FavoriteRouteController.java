package com.uberplus.backend.controller;

import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.route.FavoriteRouteCreateDTO;
import com.uberplus.backend.dto.route.FavoriteRouteDTO;
import com.uberplus.backend.repository.FavoriteRouteRepository;
import com.uberplus.backend.service.FavoriteRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/favorite-routes")
@RequiredArgsConstructor
public class FavoriteRouteController {

    private final FavoriteRouteService routeService;

    // POST /api/favorite-routes
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteRouteDTO> createFavorite(@Valid @RequestBody FavoriteRouteCreateDTO request,
                                                           Authentication auth) {
        String email = auth.getName();
        FavoriteRouteDTO dto = routeService.createFavRoute(request, email);
        URI location = ServletUriComponentsBuilder
                .fromPath("/api/favorite-routes/" + dto.getId().toString())
                .build()
                .toUri();
        return ResponseEntity.created(location).body(dto);
    }

    // GET /api/favorite-routes/{id}
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteRouteDTO> getFavorite(@PathVariable Integer id) {;
        return ResponseEntity.ok(routeService.getRoute(id));
    }

    // GET /api/favorite-routes
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FavoriteRouteDTO>> getFavorites(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(routeService.getFavRoutes(email));
    }

    // DELETE /api/favorite-routes/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Integer id, Authentication auth) {
        String email = auth.getName();
        routeService.deleteFavRoute(id, email);
        return ResponseEntity.noContent().build();
    }
}
