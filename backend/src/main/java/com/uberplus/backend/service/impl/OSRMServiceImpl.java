package com.uberplus.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uberplus.backend.model.Location;
import com.uberplus.backend.service.OSRMService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OSRMServiceImpl implements OSRMService {
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String osrmBase;

    public OSRMServiceImpl(
            HttpClient http,
            ObjectMapper mapper,
            @Value("${osrm.base-url}") String osrmBase
    ) {
        this.http = http;
        this.mapper = mapper;
        this.osrmBase = osrmBase;
    }

    @Override
    public double[][] getDurationsMatrix(List<Location> starts, List<Location> ends) throws IOException, InterruptedException {

        if (starts == null || ends == null) throw new IllegalArgumentException("starts and ends must not be null");
        if (starts.isEmpty() || ends.isEmpty()) return new double[0][0];

        // Build coordinates list
        List<String> coordList = new ArrayList<>(starts.size() + ends.size());
        for (Location s : starts) coordList.add(formatLonLat(s));
        for (Location e : ends)   coordList.add(formatLonLat(e));
        String coords = String.join(";", coordList);

        // start indices [0 .. starts.size()-1]
        String sourcesParam = IntStream.range(0, starts.size())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));

        // destinations indices [starts.size() .. starts.size()+ends.size()-1]
        int destStart = starts.size();
        String destinationsParam = IntStream.range(destStart, destStart + ends.size())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=duration&sources=%s&destinations=%s",
                osrmBase, coords, sourcesParam, destinationsParam);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("OSRM returned HTTP " + resp.statusCode() + ": " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode durations = root.get("durations");
        if (durations == null || durations.isNull()) {
            throw new IOException("OSRM response missing 'durations' field. Body: " + resp.body());
        }

        int m = durations.size();
        int n = durations.get(0).size();
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            JsonNode row = durations.get(i);
            for (int j = 0; j < n; j++) {
                JsonNode cell = row.get(j);
                if (cell == null || cell.isNull()) {
                    result[i][j] = -1.0; // indicates no route
                } else {
                    result[i][j] = cell.asDouble(); // seconds
                }
            }
        }
        return result;
    }

    @Override
    public double getDuration(Location start, Location end) throws IOException, InterruptedException {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must not be null");
        }
        if (start.getLatitude() == null || start.getLongitude() == null
                || end.getLatitude() == null || end.getLongitude() == null) {
            throw new IllegalArgumentException("Location coordinates must not be null");
        }

        String coords = formatLonLat(start) + ";" + formatLonLat(end);

        String url = String.format("%s/route/v1/driving/%s?overview=false&alternatives=false&steps=false",
                osrmBase, coords);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("OSRM returned HTTP " + resp.statusCode() + ": " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode routes = root.get("routes");
        if (routes == null || !routes.isArray() || routes.isEmpty()) {
            return -1.0;
        }

        JsonNode durationNode = routes.get(0).get("duration");
        if (durationNode == null || durationNode.isNull()) {
            return -1.0;
        }

        return durationNode.asDouble(); // seconds
    }

    private static String formatLonLat(Location l) {
        return String.format("%f,%f", l.getLongitude(), l.getLatitude());
    }
}
