package com.example.mobileapp.network.dto;

import java.util.List;

public class RideHistoryResponseDto {
    public List<RideDto> rides;
    public long total;
    public int page;
    public int size;
}
