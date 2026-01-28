package com.uberplus.backend.repository;

import com.uberplus.backend.model.FavoriteRoute;
import com.uberplus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Integer> {
    List<FavoriteRoute> findByUser(User user);
    List<FavoriteRoute> findByNameAndUser(String name, User user);
}
