package com.example.mobileapp.features.shared.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;

import java.time.LocalDateTime;

public class UserRepository {
    private static UserRepository instance;
    private final SharedPreferences prefs;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private static Context appContext;
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }
    private UserRepository() {
        this.prefs = appContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        loadUser();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
        saveUser(user);
    }

    public void clearUser() {
        currentUser.setValue(null);
        prefs.edit().clear().apply();
    }

    private void saveUser(User user) {
        prefs.edit()
                .putInt("user_id", user.getId() != null ? user.getId() : -1)
                .putString("email", user.getEmail())
                .putString("first_name", user.getFirstName())
                .putString("last_name", user.getLastName())
                .putString("address", user.getAddress())
                .putString("phone_number", user.getPhoneNumber())
                .putString("profile_picture_url", user.getProfilePicture())
                .putString("role", user.getRole() != null ? user.getRole().name() : null)
                .putBoolean("blocked", user.isBlocked())
                .putString("block_reason", user.getBlockReason())
                .putBoolean("activated", user.isActivated())
                .putString("created_at", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .putString("updated_at", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .apply();
    }

    private void loadUser() {
        int id = prefs.getInt("user_id", -1);

        if (id == -1) {
            currentUser.setValue(null);
            return;
        }

        User user = new User();

        user.setId(id);
        user.setEmail(prefs.getString("email", null));
        user.setFirstName(prefs.getString("first_name", null));
        user.setLastName(prefs.getString("last_name", null));
        user.setAddress(prefs.getString("address", null));
        user.setPhoneNumber(prefs.getString("phone_number", null));
        user.setProfilePicture(prefs.getString("profile_picture_url", null));

        String roleStr = prefs.getString("role", null);
        if (roleStr != null) {
            try {
                user.setRole(UserRole.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(null);
            }
        }

        user.setBlocked(prefs.getBoolean("blocked", false));
        user.setBlockReason(prefs.getString("block_reason", null));
        user.setActivated(prefs.getBoolean("activated", false));

        String createdAtStr = prefs.getString("created_at", null);
        if (createdAtStr != null) {
            try {
                user.setCreatedAt(LocalDateTime.parse(createdAtStr));
            } catch (Exception e) {
                user.setCreatedAt(null);
            }
        }

        String updatedAtStr = prefs.getString("updated_at", null);
        if (updatedAtStr != null) {
            try {
                user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
            } catch (Exception e) {
                user.setUpdatedAt(null);
            }
        }

        currentUser.setValue(user);
    }
}
