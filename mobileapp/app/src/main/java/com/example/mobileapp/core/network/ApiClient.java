package com.example.mobileapp.core.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "http://192.168.50.211:8080/";
    // private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/";

    private static Retrofit retrofit;
    private static Retrofit osrmRetrofit;

    private ApiClient() {}

    // backend retrofit
    public static Retrofit get() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // osrm retrofit
    public static Retrofit getOsrm() {
        if (osrmRetrofit == null) {
            osrmRetrofit = new Retrofit.Builder()
                    .baseUrl(OSRM_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return osrmRetrofit;
    }
}
