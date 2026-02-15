package com.example.mobileapp.features.shared.input;

import android.content.Context;
import android.graphics.Color;
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
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.UserApi;
import com.example.mobileapp.features.shared.models.User;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSearchInputView extends FrameLayout {
    private EditText searchEditText;
    private RecyclerView resultsContainer;
    private final List<User> results = new ArrayList<>();
    private UserSearchAdapter adapter;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private User selectedUser;
    private Runnable onUserClickCallback;



    public UserSearchInputView(Context context) {
        super(context);
        init(context);
    }

    public UserSearchInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserSearchInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_user_search, this, true);
        searchEditText = findViewById(R.id.search_edit_text);
        resultsContainer = findViewById(R.id.results_container);

        resultsContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserSearchAdapter(results, result -> {
            selectedUser = result;
            adapter.setSelectedUser(result);
            if (onUserClickCallback != null) {
                onUserClickCallback.run();
            }
        });
        resultsContainer.setAdapter(adapter);

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
                pendingSearch = () -> search(s.toString());
                debounceHandler.postDelayed(pendingSearch, 400);
            }
        });
    }

    private void search(String query) {
        if (query.length() < 3) {
            return;
        }

        ApiClient.get().create(UserApi.class).searchUsers(query, 10, 0).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(!response.isSuccessful() || response.body() == null) {
                    showMessage("Search failed: " + response.code(), false);
                    return;
                }
                results.clear();
                results.addAll(response.body());
                adapter.notifyDataSetChanged();

                if(response.body().isEmpty()) {
                    findViewById(R.id.empty_text_view).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.empty_text_view).setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showMessage("Search failed: " + t.getMessage(), false);
            }
        });
    }

    public User getSelectedUser(){
        return selectedUser;
    }

    public void setSelectedUser(User user) {
        selectedUser = user;
        adapter.setSelectedUser(user);
    }
    private void showMessage(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(isSuccess ?
                getResources().getColor(R.color.completed_ride) :
                getResources().getColor(com.google.android.material.R.color.design_default_color_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }
    public void setOnUserClickCallback(Runnable callback) {
        this.onUserClickCallback = callback;
    }
}
