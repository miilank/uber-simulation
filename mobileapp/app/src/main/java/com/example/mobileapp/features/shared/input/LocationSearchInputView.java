package com.example.mobileapp.features.shared.input;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

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

public class LocationSearchInputView extends FrameLayout {
    private EditText searchEditText;
    private RecyclerView autocompleteList;

    private GeocodingApi nominatimApi;
    private List<GeocodeResult> results = new ArrayList<>();
    private LocationAdapter adapter;
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private boolean suppressTextWatcher = false;

    public interface OnLocationSelectedListener {
        void onLocationSelected(GeocodeResult location);
    }

    private OnLocationSelectedListener listener;

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    public LocationSearchInputView(Context context) {
        super(context);
        init(context);
    }

    public LocationSearchInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LocationSearchInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.fragment_location_search, this, true);
        searchEditText = findViewById(R.id.search_input);
        autocompleteList = findViewById(R.id.autocomplete_list);

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
                if(suppressTextWatcher) {
                    // Izabere se result -> upise se u tekst -> opet se pokazuje dropdown, pa mora da se ugasi ovako
                    suppressTextWatcher = false;
                    return;
                }

                debounceHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> searchNominatim(s.toString());
                debounceHandler.postDelayed(pendingSearch, 400);
            }
        });
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) hideDropdown();
        });
    }

    private void searchNominatim(String query) {
        if (query.length() < 2) {
            hideDropdown();
            return;
        }

        nominatimApi.searchAddress(query, 5)
                .enqueue(new Callback<List<GeocodeResult>>() {
                    @Override
                    public void onResponse(Call<List<GeocodeResult>> call, Response<List<GeocodeResult>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            response.body().forEach(GeocodeResult::formatAddress);
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
        suppressTextWatcher = true;
        searchEditText.setText(result.formattedResult);
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

    public void setAddress(String address) {
        if (address == null) address = "";
        suppressTextWatcher = true;
        searchEditText.setText(address);
        suppressTextWatcher = false;
        searchEditText.setSelection(searchEditText.length());
    }

    public void clearAddress() {
        setAddress("");
    }
}

