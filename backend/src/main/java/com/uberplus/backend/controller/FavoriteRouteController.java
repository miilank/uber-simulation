package com.uberplus.backend.controller;

import com.uberplus.backend.dto.route.FavoriteRouteCreateDTO;
import com.uberplus.backend.dto.route.FavoriteRouteDTO;
import com.uberplus.backend.repository.FavoriteRouteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-routes")
@RequiredArgsConstructor
public class FavoriteRouteController {

    private final FavoriteRouteRepository favoriteRouteRepository;

    // POST /api/favorite-routes
    @PostMapping
    public ResponseEntity<FavoriteRouteDTO> createFavorite(@Valid @RequestBody FavoriteRouteCreateDTO request) {
        return ResponseEntity.ok(new FavoriteRouteDTO());
    }

    // GET /api/favorite-routes
    @GetMapping
    public ResponseEntity<List<FavoriteRouteDTO>> getFavorites() {
        return ResponseEntity.ok(List.of(new FavoriteRouteDTO(), new FavoriteRouteDTO()));
    }
}
