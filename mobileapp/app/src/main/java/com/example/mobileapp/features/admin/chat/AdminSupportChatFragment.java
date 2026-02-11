package com.example.mobileapp.features.admin.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.ChatApi;
import com.example.mobileapp.features.shared.api.dto.ConversationPreviewDto;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.example.mobileapp.features.shared.services.WebSocketManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSupportChatFragment extends Fragment
        implements ConversationAdapter.OnConversationClickListener, WebSocketManager.MessageListener {

    private static final String TAG = "AdminSupportChat";

    private RecyclerView conversationsRecyclerView;
    private ConversationAdapter conversationAdapter;
    private TextView emptyTextView;

    private ChatApi chatApi;
    private WebSocketManager webSocketManager;
    private Integer currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_support_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        // Get current user
        UserRepository.getInstance().getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getId() != null) {
                currentUserId = user.getId();

                // Initialize API
                chatApi = ApiClient.get().create(ChatApi.class);

                // Load conversations
                loadConversations();

                // Connect WebSocket for real-time updates
                connectWebSocket();
            }
        });
    }

    private void initViews(View view) {
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(this);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        conversationsRecyclerView.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        chatApi.getConversations().enqueue(new Callback<List<ConversationPreviewDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConversationPreviewDto>> call, @NonNull Response<List<ConversationPreviewDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationPreviewDto> conversations = response.body();
                    conversationAdapter.setConversations(conversations);

                    // Show/hide empty state
                    if (conversations.isEmpty()) {
                        conversationsRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        conversationsRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConversationPreviewDto>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load conversations", t);
            }
        });
    }

    @Override
    public void onConversationClick(Integer userId) {
        // Open chat with this user
        AdminChatDetailFragment chatFragment = AdminChatDetailFragment.newInstance(userId);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void connectWebSocket() {
        if (currentUserId == null) return;

        webSocketManager = new WebSocketManager(requireContext());
        webSocketManager.connect(currentUserId, this);
    }

    // WebSocket listeners - reload conversations when new message arrives
    @Override
    public void onMessageReceived(com.example.mobileapp.features.shared.api.dto.ChatMessageDto message) {
        requireActivity().runOnUiThread(() -> loadConversations());
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "WebSocket connected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "WebSocket disconnected");
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}