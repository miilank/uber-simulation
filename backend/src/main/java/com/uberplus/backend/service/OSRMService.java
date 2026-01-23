package com.uberplus.backend.service;

import com.uberplus.backend.model.Location;

import java.io.IOException;
import java.util.List;

public interface OSRMService {
    double[][] getDurationsMatrix(List<Location> starts, List<Location> ends) throws IOException, InterruptedException;
    double getDuration(Location start, Location end) throws IOException, InterruptedException;
}
