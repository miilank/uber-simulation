package com.example.mobileapp.features.shared.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.VehicleMarker;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.VehiclesApi;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {

    private WebView webView;
    private VehiclesApi vehiclesApi;
    private final Gson gson = new Gson();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            fetchVehicles();
            handler.postDelayed(this, 5000);
        }
    };

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        webView = v.findViewById(R.id.mapWebView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Load Leaflet from assets
        webView.loadUrl("file:///android_asset/map/index.html");

        vehiclesApi = ApiClient.get().create(VehiclesApi.class);

        webView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN ||
                    event.getActionMasked() == android.view.MotionEvent.ACTION_MOVE) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP ||
                    event.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                view.performClick();
            }
            return false;
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTask);
    }

    private void fetchVehicles() {
        vehiclesApi.getMapVehicles().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<VehicleMarker>> call,
                                   @NonNull Response<List<VehicleMarker>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                String json = gson.toJson(response.body());

                // escape for JS string in single-quote
                String escaped = json
                        .replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "");

                String js = "window.setVehiclesJson('" + escaped + "');";

                if (webView != null) {
                    webView.evaluateJavascript(js, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VehicleMarker>> call, @NonNull Throwable t) {
                // optional: log
            }
        });
    }
}
