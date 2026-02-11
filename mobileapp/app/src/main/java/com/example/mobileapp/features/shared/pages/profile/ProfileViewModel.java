package com.example.mobileapp.features.shared.pages.profile;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.DriversApi;
import com.example.mobileapp.features.shared.api.UserApi;
import com.example.mobileapp.features.shared.api.dto.DriverDto;
import com.example.mobileapp.features.shared.api.dto.UserUpdateRequestDto;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.example.mobileapp.features.shared.repositories.UserRepository;

import java.io.File;
import java.sql.Driver;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    public class MessageResult {
        public final String message;
        public final boolean isSuccess;

        public MessageResult(String message, boolean isSuccess) {
            this.message = message;
            this.isSuccess = isSuccess;
        }
    }

    private final UserRepository repository;
    private final UserApi userApi;
    private final DriversApi driversApi;
    private final LiveData<User> currentUser;
    private final MutableLiveData<DriverDto> driverInfo = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    public final MutableLiveData<MessageResult> message = new MutableLiveData<>();

    public ProfileViewModel() {
        this.repository = UserRepository.getInstance();
        this.userApi = ApiClient.get().create(UserApi.class);
        this.currentUser = repository.getCurrentUser();
        this.driversApi = ApiClient.get().create(DriversApi.class);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }


    public LiveData<String> getError() {
        return error;
    }

    public LiveData<DriverDto> getDriverInfo() {
        return driverInfo;
    }

    public void fetchUserFromApi() {
        error.setValue(null);

        userApi.fetchMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    repository.setCurrentUser(user);
                } else {
                    error.setValue("Failed to fetch user data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchDriverInfo() {
        error.setValue(null);

        driversApi.fetchMe().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DriverDto> call, @NonNull Response<DriverDto> response) {

                if (response.isSuccessful() && response.body() != null) {
                    driverInfo.setValue(response.body());
                } else {
                    error.setValue("Failed to fetch user data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DriverDto> call, @NonNull Throwable t) {
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }
    public void updateUser(String firstName, String lastName, String address, String phone,
                           MultipartBody.Part profileImage) {
        error.setValue(null);

        String errorMsg = validateUser(firstName, lastName, address, phone);


        if (errorMsg != null) {
            error.setValue(errorMsg);
            return;
        }

        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setFirstName(firstName);
        updateRequestDto.setLastName(lastName);
        updateRequestDto.setAddress(address);
        updateRequestDto.setPhoneNumber(phone);

        if(currentUser.getValue().getRole() == UserRole.DRIVER) {
            driversApi.updateProfile(updateRequestDto, profileImage).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        message.setValue(new MessageResult("Request sent successfully. Changes will be visible once they are verified.",
                                true));
                    } else {
                        message.setValue(new MessageResult("Update failed", false));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    message.setValue(new MessageResult("Network error:" + t.getMessage(), false));
                }
            });
        } else {
            userApi.updateProfile(updateRequestDto, profileImage)
                    .enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                User updatedUser = response.body();
                                repository.setCurrentUser(updatedUser);
                                message.setValue(new MessageResult("Profile updated successfully", true));
                            } else {
                                error.setValue("Failed to update profile");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                            error.setValue("Network error: " + t.getMessage());
                        }
                    });
        }
    }

    private String validateUser(
            String first,
            String last,
            String address,
            String phone
    ) {
        if (first == null || first.isEmpty()) {
            return "Please enter a first name.";
        }
        if (last == null || last.isEmpty()) {
            return "Please enter a last name.";
        }
        if (address == null || address.isEmpty()) {
            return "Please enter an address.";
        }
        if (phone == null || phone.isEmpty()) {
            return "Please enter a phone number.";
        }
        if (!isPhoneValid(phone)) {
            return "Invalid phone number.";
        }

        return null;
    }


    private boolean isPhoneValid(String phone) {
        if (phone == null) return false;
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < 7) return false;

        // digits, +, -, space, parentheses, dot
        String allowed = "^[\\d+\\-\\s().]+$";
        return Pattern.compile(allowed).matcher(phone).matches();
    }

}
