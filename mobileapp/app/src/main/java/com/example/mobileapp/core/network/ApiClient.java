package com.example.mobileapp.core.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "http://192.168.0.10:8080/";
    // private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/";

    private static Retrofit retrofit;
    private static Retrofit osrmRetrofit;
    private static Context appContext = null;

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }
    private ApiClient() {}

    // backend retrofit
    public static Retrofit get() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                                    LocalDateTime.parse(json.getAsString(),
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                    new com.google.gson.JsonPrimitive(src.format(
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(appContext))
                    .build();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
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
