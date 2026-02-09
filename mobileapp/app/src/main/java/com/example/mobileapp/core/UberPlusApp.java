package com.example.mobileapp.core;

import android.app.Application;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.repositories.UserRepository;

public class UberPlusApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApiClient.initialize(this);
        UserRepository.initialize(this);
    }
}
