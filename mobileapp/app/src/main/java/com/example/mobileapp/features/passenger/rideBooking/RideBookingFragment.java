package com.example.mobileapp.features.passenger.rideBooking;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.passenger.dashboard.UserDashboardFragment;
import com.example.mobileapp.features.shared.api.FavoriteRouteApi;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.RoutingApi;
import com.example.mobileapp.features.shared.api.dto.CreateRideRequestDto;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteCreateDto;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.OsrmRouteResponse;
import com.example.mobileapp.features.shared.api.dto.RideDto;
import com.example.mobileapp.features.shared.input.LocationSearchInputView;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.models.enums.VehicleType;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideBookingFragment extends Fragment {

    private FrameLayout successOverlay;
    private LinearLayout step1, step2, step3;
    private RecyclerView stopsContainer, passengersContainer;
    private EditText etDate, etTime, inputFavoriteRouteName;
    private AppCompatImageButton btnAddStop, btnAddPassenger;
    private ImageView btnDateIcon, btnTimeIcon, profileImage;
    private TextView userEmail, successMessage;
    private AutoCompleteTextView actvFavoriteRoute, actvVehicleType;
    private AppCompatButton btnStep1Next, btnStep1Cancel, btnStep2Next, btnStep2Back, btnSaveRoute,
            btnStep3Book, btnStep3Back, btnSuccessExit;

    private MaterialCheckBox checkboxInfants, checkboxPets;

    RideBookingLocationAdapter locationAdapter;
    RideBookingPassengerAdapter passengerAdapter;
    List<LocationDto> waypoints = new ArrayList<>();
    List<String> passengers = new ArrayList<>();
    LocationDto startLocation;
    LocationDto endLocation;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationAdapter = new RideBookingLocationAdapter(waypoints,
                (index, location) -> {
            waypoints.set(index, location);
            View current = requireActivity().getCurrentFocus();
            if (current != null) current.clearFocus();
            locationAdapter.notifyItemChanged(index);
            renderRoute();
        }, (index) -> {
            waypoints.remove(index);
            View current = requireActivity().getCurrentFocus();
            if (current != null) current.clearFocus();
            locationAdapter.notifyItemRemoved(index);
            renderRoute();
        });

        passengerAdapter = new RideBookingPassengerAdapter(passengers, (index, passenger) -> {
            passengers.set(index, passenger);
            View current = requireActivity().getCurrentFocus();
            if (current != null) current.clearFocus();
            passengerAdapter.notifyItemChanged(index);
        }, (index) -> {
            passengers.remove(index);
            View current = requireActivity().getCurrentFocus();
            if (current != null) current.clearFocus();
            passengerAdapter.notifyItemRemoved(index);
        });
    }

    @Override
    public View onCreateView(
            @NonNull
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_ride_booking, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        stopsContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        stopsContainer.setAdapter(locationAdapter);

        passengersContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        passengersContainer.setAdapter(passengerAdapter);

        initListeners(view);

        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }

    }

    private void initViews(View view) {
        // Step containers
        step1 = view.findViewById(R.id.step1);
        step2 = view.findViewById(R.id.step2);
        step3 = view.findViewById(R.id.step3);

        // Step 1
        stopsContainer = view.findViewById(R.id.stops_container);
        btnAddStop = view.findViewById(R.id.btn_add_stop);
        btnDateIcon = view.findViewById(R.id.btn_date_icon);
        btnTimeIcon = view.findViewById(R.id.btn_time_icon);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);

        LocalDateTime now = LocalDateTime.now();

        etDate.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())));
        etTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())));

        getFavoriteRoutes(view);

        actvFavoriteRoute = view.findViewById(R.id.actvFavoriteRoute);
        btnStep1Next = view.findViewById(R.id.btn_step1_next);
        btnStep1Cancel = view.findViewById(R.id.btn_step1_cancel);

        // Step 2
        profileImage = view.findViewById(R.id.profile_image);
        userEmail = view.findViewById(R.id.user_email);
        passengersContainer = view.findViewById(R.id.passengers_container);
        btnAddPassenger = view.findViewById(R.id.btn_add_passenger);
        btnStep2Next = view.findViewById(R.id.btn_step2_next);
        btnStep2Back = view.findViewById(R.id.btn_step2_back);

        UserRepository.getInstance().getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            userEmail.setText(user.getEmail());
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfilePicture() + "?cb=" + LocalDateTime.now().toString())
                        .placeholder(R.drawable.img_defaultprofile)
                        .error(R.drawable.img_defaultprofile)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.img_defaultprofile);
            }
        });

        // Step 3
        checkboxPets = view.findViewById(R.id.checkbox_pets);
        checkboxInfants = view.findViewById(R.id.checkbox_infants);

        List<String> displayItems = new ArrayList<>();
        displayItems.add("Any");
        for (VehicleType vt : VehicleType.values()) {
            displayItems.add(vt.getDisplayName());
        }

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                displayItems
        );
        actvVehicleType = view.findViewById(R.id.actvVehicleType);
        actvVehicleType.setAdapter(vehicleAdapter);
        actvVehicleType.setText(displayItems.get(0), false);

        inputFavoriteRouteName = view.findViewById(R.id.input_favorite_route_name);
        btnSaveRoute = view.findViewById(R.id.btn_save_route);
        btnStep3Book = view.findViewById(R.id.btn_step3_book);
        btnStep3Back = view.findViewById(R.id.btn_step3_back);

        // Success overlay
        successOverlay = view.findViewById(R.id.success_overlay);
        successMessage = view.findViewById(R.id.success_message);
        btnSuccessExit = view.findViewById(R.id.btn_success_exit);
    }

    private void initListeners(View view) {
        // Step 1
        btnAddStop.setOnClickListener(v -> {
            waypoints.add(new LocationDto());
            locationAdapter.notifyItemInserted(waypoints.size()-1);
        });

        btnAddPassenger.setOnClickListener(v -> {
            passengers.add("");
            passengerAdapter.notifyItemInserted(passengers.size()-1);
        });

        LocationSearchInputView pickup = view.findViewById(R.id.pickup);
        pickup.setOnLocationSelectedListener((geocodeResult) -> {
            LocationDto selected = new LocationDto();
            selected.setAddress(geocodeResult.formattedResult);
            selected.setLatitude(geocodeResult.lat);
            selected.setLongitude(geocodeResult.lon);

            startLocation = selected;
            renderRoute();
        });

        LocationSearchInputView destination = view.findViewById(R.id.destination);
        destination.setOnLocationSelectedListener((geocodeResult) -> {
            LocationDto selected = new LocationDto();
            selected.setAddress(geocodeResult.formattedResult);
            selected.setLatitude(geocodeResult.lat);
            selected.setLongitude(geocodeResult.lon);

            endLocation = selected;
            renderRoute();
        });

        btnDateIcon.setOnClickListener(v -> showDatePicker(etDate));
        btnTimeIcon.setOnClickListener(v -> showTimePicker(etTime));

        // Navigation
        btnStep1Cancel.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new UserDashboardFragment())
                .commit());

        btnStep1Next.setOnClickListener(v -> {
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
        });

        btnStep2Back.setOnClickListener(v -> {
            step1.setVisibility(View.VISIBLE);
            step2.setVisibility(View.GONE);
        });

        btnStep2Next.setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
        });

        btnStep3Back.setOnClickListener(v -> {
            step2.setVisibility(View.VISIBLE);
            step3.setVisibility(View.GONE);
        });

        btnStep3Book.setOnClickListener(v -> finishBooking());

        btnSuccessExit.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new UserDashboardFragment())
                .commit());

        // Route
        btnSaveRoute.setOnClickListener(v -> saveFavoriteRoute());
    }

    private void finishBooking() {
        if(!validateInputs()) {
            return;
        }

        CreateRideRequestDto request = new CreateRideRequestDto();
        request.babyFriendly = checkboxInfants.isChecked();
        request.petFriendly = checkboxPets.isChecked();
        request.startLocation = startLocation;
        request.endLocation = endLocation;
        request.waypoints = waypoints;
        request.linkedPassengerEmails = passengers;
        request.scheduledTime = parseDateTimeFromStrings(etDate.getText().toString(),
                etTime.getText().toString());
        request.vehicleType = getSelectedVehicleType();

        ApiClient.getOsrm().create(RoutingApi.class)
                .route(buildOsrmCoords(startLocation, waypoints, endLocation), "full", "geojson")
                .enqueue(new Callback<OsrmRouteResponse>() {
                    @Override
                    public void onResponse(Call<OsrmRouteResponse> call, Response<OsrmRouteResponse> response) {
                        if(response.body() == null || !response.isSuccessful()) {
                            showMessage("Failed to book. Please try again later.", false);
                            return;
                        }

                        request.distanceKm = response.body().routes.get(0).distance / 1000;
                        request.estimatedDurationMinutes = (int) response.body().routes.get(0).duration / 60;

                        ApiClient.get().create(RidesApi.class).requestRide(request).enqueue(new Callback<RideDto>() {
                            @Override
                            public void onResponse(@NonNull Call<RideDto> call, @NonNull Response<RideDto> response) {
                                if(response.body() == null || !response.isSuccessful()) {
                                    showMessage("Failed to book. Please try again later.", false);
                                    return;
                                }

                                DateTimeFormatter timeFormatter =
                                        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

                                String formattedTime = response.body()
                                        .estimatedStartTime
                                        .format(timeFormatter);

                                String message = String.format(
                                        Locale.getDefault(),
                                        "Ride successfully assigned to driver: %s! " +
                                                "Car is expected to arrive at %s. Price is: €%.2f.",
                                        response.body().driverEmail,
                                        formattedTime,
                                        response.body().basePrice
                                );
                                successMessage.setText(message);
                                successOverlay.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailure(@NonNull Call<RideDto> call, @NonNull Throwable t) {
                                showMessage("Failed to book ride: " + t.getMessage(), false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<OsrmRouteResponse> call, Throwable t) {
                        showMessage("Failed to calculate route: " + t.getMessage(), false);
                    }
                });

    }

    private boolean validateInputs() {
        if(!validateWaypoints()) {
            return false;
        }

        for (int i = 0; i < passengers.size(); i++) {
            String p = passengers.get(i);
            if (p == null || p.trim().isEmpty()) {
                showMessage("Passenger " + (i + 1) + " is empty. Please enter an email.", false);
                return false;
            }
        }

        String dateStr = etDate != null && etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String timeStr = etTime != null && etTime.getText() != null ? etTime.getText().toString().trim() : "";

        LocalDateTime scheduled;
        try {
            scheduled = parseDateTimeFromStrings(dateStr, timeStr);
        } catch (DateTimeParseException ex) {
            showMessage("Invalid date or time format. Please use the displayed format.", false);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (scheduled.isBefore(now.minusMinutes(5))) {
            showMessage("Scheduled time must be in the future.", false);
            return false;
        }
        if (scheduled.isAfter(now.plusHours(5))) {
            showMessage("Scheduled time must be within 5 hours from now.", false);
            return false;
        }

        return true;
    }

    private boolean validateWaypoints() {
        if (startLocation == null || endLocation == null) {
            showMessage("Please select both pickup and destination.", false);
            return false;
        }
        boolean hasNullWaypoint = waypoints.stream().anyMatch(location -> (location.getLatitude()==null));
        if (hasNullWaypoint) {
            showMessage("Please fill all stops or remove empty stops.", false);
            return false;
        }

        return true;
    }

    private void getFavoriteRoutes(View fragmentView) {
        ApiClient.get().create(FavoriteRouteApi.class).getFavoriteRoutes().enqueue(new Callback<List<FavoriteRouteDto>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<FavoriteRouteDto>> call, Response<List<FavoriteRouteDto>> response) {
                if(response.body() == null || response.body().isEmpty()) {
                    return;
                }

                ArrayAdapter<FavoriteRouteDto> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        response.body()
                );
                actvFavoriteRoute.setAdapter(adapter);

                actvFavoriteRoute.setOnItemClickListener((parent, view, position, id) -> {
                    FavoriteRouteDto chosen = adapter.getItem(position);
                    startLocation = chosen.getStartLocation();
                    LocationSearchInputView pickup = fragmentView.findViewById(R.id.pickup);
                    pickup.setAddress(startLocation.getAddress());

                    endLocation = chosen.getEndLocation();
                    LocationSearchInputView destination = fragmentView.findViewById(R.id.destination);
                    destination.setAddress(endLocation.getAddress());

                    waypoints.clear();
                    if (chosen.getWaypoints() != null) {
                        waypoints.addAll(chosen.getWaypoints());
                    }
                    locationAdapter.notifyDataSetChanged();
                    checkboxInfants.setChecked(chosen.isPetsFriendly());
                    checkboxInfants.setChecked(chosen.isBabyFriendly());
                    if(chosen.getVehicleType() != null) {
                        actvVehicleType.setText(chosen.getVehicleType().getDisplayName());
                    } else {
                        actvVehicleType.setText("Any");
                    }
                });
            }

            @Override
            public void onFailure(Call<List<FavoriteRouteDto>> call, Throwable t) {

            }
        });
    }

    private void saveFavoriteRoute() {
        if(!validateWaypoints()) {
            return;
        }

        if(inputFavoriteRouteName.getText().toString().trim().isEmpty()) {
            showMessage("Route name cannot be empty.", false);
            return;
        }

        FavoriteRouteCreateDto dto = new FavoriteRouteCreateDto();
        dto.name = inputFavoriteRouteName.getText().toString().trim();
        dto.babyFriendly = checkboxInfants.isChecked();
        dto.petsFriendly = checkboxPets.isChecked();
        dto.vehicleType = getSelectedVehicleType();
        dto.startLocation = startLocation;
        dto.endLocation = endLocation;
        dto.waypoints = waypoints;

        ApiClient.get().create(FavoriteRouteApi.class).createFavoriteRoute(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()) {
                    showMessage("Failed to create route, error: " + response.code(), false);
                    return;
                }

                showMessage("Route successfully created!", true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showMessage("Failed to create route: " + t.getMessage(), false);
            }
        });
    }

    private void renderRoute() {
        MapFragment map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);

        boolean emptyWaypoint = waypoints.stream().anyMatch(location -> location.getLatitude()==null);

        if (map==null) return;

        if (startLocation==null || endLocation==null || emptyWaypoint)
        {
            map.clearRouteOnMap();
            return;
        }

        List<MapFragment.RoutePoint> points = new ArrayList<>();
        points.add(locationToPoint(startLocation));
        waypoints.forEach((locationDto -> points.add(locationToPoint(locationDto))));
        points.add(locationToPoint(endLocation));

        map.setRoutePoints(points);
    }

    private MapFragment.RoutePoint locationToPoint(LocationDto location) {
        return new MapFragment.RoutePoint(location.getLatitude(), location.getLongitude(), "");
    }


    private void showDatePicker(android.widget.EditText target) {
        if (target == null) return;

        hideKeyboard(target);
        java.util.Calendar c = java.util.Calendar.getInstance();

        @SuppressLint("SetTextI18n")
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialog,
                (view, year, month, dayOfMonth) -> {
                    String dd = String.format(java.util.Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(java.util.Locale.getDefault(), "%02d", month + 1);
                    String yyyy = String.valueOf(year);
                    target.setText(dd + "/" + mm + "/" + yyyy);
                },
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        );

        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.app_accent
            );
            android.widget.Button positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            android.widget.Button negative = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);

            if (positive != null) positive.setTextColor(accent);
            if (negative != null) negative.setTextColor(accent);
        });

        dialog.show();
    }

    private void showTimePicker(EditText target) {
        if (target == null) return;

        hideKeyboard(target);

        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        @SuppressLint("SetTextI18n") TimePickerDialog dialog = new TimePickerDialog(requireContext(), R.style.MyDatePickerDialog,
                (view, hourOfDay, minute) -> etTime.setText(hourOfDay + ":" + minute), mHour, mMinute, false);

        dialog.setOnShowListener(d -> {
            int dark = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.app_dark
            );
            android.widget.Button positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            android.widget.Button negative = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);

            if (positive != null) positive.setTextColor(dark);
            if (negative != null) negative.setTextColor(dark);
        });

        dialog.show();
    }

    private String buildOsrmCoords(LocationDto start, List<LocationDto> waypoints, LocationDto end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must be non-null");
        }

        StringBuilder sb = new StringBuilder(128);
        java.util.function.BiConsumer<StringBuilder, LocationDto> appendPoint = (builder, loc) -> {
            if (loc.getLatitude() == null || loc.getLongitude() == null) {
                showMessage("Failed to calculate route because of null address.", false);
                return;
            }
            builder.append(String.format(Locale.US, "%.6f,%.6f", loc.getLongitude(), loc.getLatitude()));
        };

        appendPoint.accept(sb, start);

        if (waypoints != null) {
            for (LocationDto wp : waypoints) {
                sb.append(';');
                appendPoint.accept(sb, wp);
            }
        }

        sb.append(';');
        appendPoint.accept(sb, end);

        return sb.toString();
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private VehicleType getSelectedVehicleType() {
        String selected = (actvVehicleType.getText() == null || actvVehicleType.getText().toString().equals("Any")) ?
                null : actvVehicleType.getText().toString().trim();
        return VehicleType.fromDisplayName(selected);
    }


    public static LocalDateTime parseDateTimeFromStrings(String dateStr, String timeStr) throws DateTimeParseException {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault());
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:m", Locale.getDefault());

        LocalDate date = LocalDate.parse(dateStr.trim(), dateFmt);
        LocalTime time = LocalTime.parse(timeStr.trim(), timeFmt);

        return LocalDateTime.of(date, time);
    }

    private void showMessage(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(isSuccess ?
                getResources().getColor(R.color.completed_ride) :
                getResources().getColor(com.google.android.material.R.color.design_default_color_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
