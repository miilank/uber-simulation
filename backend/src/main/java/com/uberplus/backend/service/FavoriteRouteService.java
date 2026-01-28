package com.uberplus.backend.service;

import com.uberplus.backend.dto.route.FavoriteRouteCreateDTO;
import com.uberplus.backend.dto.route.FavoriteRouteDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface FavoriteRouteService {
    List<FavoriteRouteDTO> getFavRoutes(String email);

    FavoriteRouteDTO createFavRoute(@Valid FavoriteRouteCreateDTO request, String email);

    FavoriteRouteDTO getRoute(Integer routeId);

    void deleteFavRoute(Integer id, String email);
}
