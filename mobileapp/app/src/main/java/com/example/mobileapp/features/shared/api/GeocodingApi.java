package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.GeocodeResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApi {
    @GET("search")
    Call<List<GeocodeResult>> searchAddressInternal(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int details,
            @Query("countrycodes") String countryCodes,
            @Query("layer") String layer,
            @Query("viewbox") String viewbox,
            @Query("limit") int limit
    );
    @GET("reverse")
    Call<GeocodeResult> reverseGeocodeInternal(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("format") String format,
            @Query("addressdetails") int details
    );
    default Call<List<GeocodeResult>> searchAddress(
            String query,
            int limit
    ) {
        return searchAddressInternal(
                query,
                "jsonv2",
                1,
                "rs",
                "address",
                "19.708797,45.316329,19.958903,45.199067",
                limit
        );
    }
    default Call<GeocodeResult> reverseGeocode(
            double latitude,
            double longitude
    ) {
        return reverseGeocodeInternal(
                latitude,
                longitude,
                "jsonv2",
                1
        );
    }
}

