package com.example.mobileapp.features.shared.input;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.GeocodingApi;
import com.example.mobileapp.features.shared.api.dto.GeocodeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationSearchInputFragment extends Fragment {
    private EditText searchEditText;
    private RecyclerView autocompleteList;

    private GeocodingApi nominatimApi;
    private List<GeocodeResult> results = new ArrayList<>();
    private LocationAdapter adapter;
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    public interface OnLocationSelectedListener {
        void onLocationSelected(GeocodeResult location);
    }

    private OnLocationSelectedListener listener;

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_search, container, false);

        searchEditText = view.findViewById(R.id.search_input);
        autocompleteList = view.findViewById(R.id.autocomplete_list);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "UberPlusAndroid")
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        nominatimApi = retrofit.create(GeocodingApi.class);

        autocompleteList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocationAdapter(results, result -> onLocationSelected(result));
        autocompleteList.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                debounceHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> searchNominatim(s.toString());
                debounceHandler.postDelayed(pendingSearch, 400);
            }
        });
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) hideDropdown();
        });

        return view;
    }

    private void searchNominatim(String query) {
        if (query.length() < 2) {
            hideDropdown();
            return;
        }

        nominatimApi.searchAddress(query + ", RS", "json", 5, 1)
                .enqueue(new Callback<List<GeocodeResult>>() {
                    @Override
                    public void onResponse(Call<List<GeocodeResult>> call, Response<List<GeocodeResult>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            results.clear();
                            results.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            autocompleteList.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GeocodeResult>> call, Throwable t) {
                        results.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void onLocationSelected(GeocodeResult result) {
        searchEditText.setText(result.display_name);
        hideDropdown();
        if (listener != null) listener.onLocationSelected(result);
    }

    private void hideDropdown() {
        autocompleteList.setVisibility(View.GONE);
    }

    public String getAddress(){
        String address = String.valueOf(searchEditText.getText());
        return address;
    }
}

