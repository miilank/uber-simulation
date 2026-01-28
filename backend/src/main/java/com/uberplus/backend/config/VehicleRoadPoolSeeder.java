package com.uberplus.backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uberplus.backend.model.Location;
import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class VehicleRoadPoolSeeder implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Value("${uberplus.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${uberplus.seed.pool-size:200}")
    private int poolSize;

    @Value("${uberplus.seed.bbox.minLat:45.2271}") private double minLat;
    @Value("${uberplus.seed.bbox.maxLat:45.3071}") private double maxLat;
    @Value("${uberplus.seed.bbox.minLng:19.7735}") private double minLng;
    @Value("${uberplus.seed.bbox.maxLng:19.8935}") private double maxLng;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) {
        List<Vehicle> vehicles = vehicleRepository.findByStatusIn(
                List.of(VehicleStatus.AVAILABLE, VehicleStatus.OCCUPIED)
        );
        System.out.println("[VehicleRoadPoolSeeder] vehicles found: " + vehicles.size());

        if (vehicles.isEmpty()) {
            System.out.println("[VehicleRoadPoolSeeder] No vehicles found with AVAILABLE/OCCUPIED. Nothing to update.");
            return;
        }

        // make the road pool
        Random rnd = new Random();
        List<double[]> pool = buildRoadPool(poolSize, rnd);
        System.out.println("[VehicleRoadPoolSeeder] pool size: " + pool.size());

        // save locations to vehicles
        int updated = 0;
        for (Vehicle v : vehicles) {
            double[] p = pool.get(rnd.nextInt(pool.size()));
            v.setCurrentLocation(new Location(
                    null,
                    p[0],
                    p[1],
                    "Novi Sad",
                    LocalDateTime.now()
            ));
            updated++;
        }

        vehicleRepository.saveAll(vehicles);
        System.out.println("[VehicleRoadPoolSeeder] UPDATED vehicles: " + updated);
        System.out.println("[VehicleRoadPoolSeeder] DONE");
    }

    private List<double[]> buildRoadPool(int targetSize, Random rnd) {
        Set<String> seen = new HashSet<>();
        List<double[]> pool = new ArrayList<>(targetSize);

        int attempts = 0;
        int maxAttempts = Math.max(targetSize * 10, 200);

        while (pool.size() < targetSize && attempts < maxAttempts) {
            attempts++;

            double lat = minLat + rnd.nextDouble() * (maxLat - minLat);
            double lng = minLng + rnd.nextDouble() * (maxLng - minLng);

            double[] snapped = snapToRoad(lat, lng);

            String key = String.format(Locale.US, "%.5f,%.5f", snapped[0], snapped[1]);
            if (seen.add(key)) pool.add(snapped);

            if (attempts % 8 == 0) {
                try { Thread.sleep(40); } catch (InterruptedException ignored) {}
            }
        }

        if (pool.isEmpty()) {
            System.out.println("[VehicleRoadPoolSeeder] WARNING: OSRM pool is empty. Falling back to random points (not on roads).");
            for (int i = 0; i < Math.min(50, targetSize); i++) {
                double lat = minLat + rnd.nextDouble() * (maxLat - minLat);
                double lng = minLng + rnd.nextDouble() * (maxLng - minLng);
                pool.add(new double[]{lat, lng});
            }
        }

        return pool;
    }

    private double[] snapToRoad(double lat, double lng) {
        try {
            String url = String.format(
                    Locale.US,
                    "%s/nearest/v1/driving/%f,%f?number=1",
                    osrmBaseUrl, lng, lat
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[OSRM] status=" + resp.statusCode());
            if (resp.statusCode() != 200) {
                System.out.println("[OSRM] body=" + resp.body());
                return new double[]{lat, lng};
            }

            JsonNode root = om.readTree(resp.body());
            JsonNode loc = root.path("waypoints").path(0).path("location"); // [lon, lat]
            if (!loc.isArray() || loc.size() < 2) {
                return new double[]{lat, lng};
            }


            double sLng = loc.get(0).asDouble();
            double sLat = loc.get(1).asDouble();
            return new double[]{sLat, sLng};
        } catch (Exception e) {
            System.out.println("[OSRM] ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return new double[]{lat, lng};
        }
    }
}
