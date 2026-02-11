package com.example.mobileapp.features.admin.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.ChatApi;
import com.example.mobileapp.features.shared.api.dto.ChatHistoryDto;
import com.example.mobileapp.features.shared.api.dto.ChatMessageCreateDto;
import com.example.mobileapp.features.shared.api.dto.ChatMessageDto;
import com.example.mobileapp.features.shared.chat.ChatAdapter;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.example.mobileapp.features.shared.services.WebSocketManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatDetailFragment extends Fragment implements WebSocketManager.MessageListener {

    private static final String TAG = "AdminChatDetail";
    private static final String ARG_USER_ID = "user_id";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView headerTitle;

    private ChatAdapter chatAdapter;
    private ChatApi chatApi;
    private WebSocketManager webSocketManager;
    private ChatHistoryDto chatHistory;
    private Integer currentUserId;
    private Integer otherUserId;

    public static AdminChatDetailFragment newInstance(Integer userId) {
        AdminChatDetailFragment fragment = new AdminChatDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_chat_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get user ID from arguments
        if (getArguments() != null) {
            otherUserId = getArguments().getInt(ARG_USER_ID);
        }

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        // Get current user
        UserRepository.getInstance().getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getId() != null) {
                currentUserId = user.getId();
                chatAdapter = new ChatAdapter(currentUserId);
                messagesRecyclerView.setAdapter(chatAdapter);

                // Initialize API
                chatApi = ApiClient.get().create(ChatApi.class);

                // Load chat history
                loadChatHistory();

                // Connect WebSocket
                connectWebSocket();
            }
        });
    }

    private void initViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        backButton = view.findViewById(R.id.backButton);
        headerTitle = view.findViewById(R.id.headerTitle);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadChatHistory() {
        chatApi.getChatHistoryWithUser(otherUserId).enqueue(new Callback<ChatHistoryDto>() {
            @Override
            public void onResponse(@NonNull Call<ChatHistoryDto> call, @NonNull Response<ChatHistoryDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatHistory = response.body();
                    headerTitle.setText(chatHistory.getOtherUserName());
                    chatAdapter.setMessages(chatHistory.getMessages());
                    scrollToBottom();

                    // Mark as read
                    if (chatHistory.getOtherUserId() != null) {
                        markAsRead(chatHistory.getOtherUserId());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatHistoryDto> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load chat history", t);
                Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectWebSocket() {
        if (currentUserId == null) return;

        webSocketManager = new WebSocketManager(requireContext());
        webSocketManager.connect(currentUserId, this);
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message) || chatHistory == null) {
            return;
        }

        ChatMessageCreateDto request = new ChatMessageCreateDto(
                chatHistory.getOtherUserId(),
                message
        );

        chatApi.sendMessage(request).enqueue(new Callback<ChatMessageDto>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessageDto> call, @NonNull Response<ChatMessageDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatAdapter.addMessage(response.body());
                    messageInput.setText("");
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessageDto> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to send message", t);
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAsRead(Integer senderId) {
        chatApi.markAsRead(senderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "Messages marked as read");
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to mark as read", t);
            }
        });
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    // WebSocketManager.MessageListener implementation
    @Override
    public void onMessageReceived(ChatMessageDto message) {
        requireActivity().runOnUiThread(() -> {
            if (chatHistory != null) {
                if (message.getSenderId().equals(chatHistory.getOtherUserId()) ||
                        message.getRecipientId().equals(chatHistory.getOtherUserId())) {
                    chatAdapter.addMessage(message);
                    scrollToBottom();

                    // Mark as read if from other user
                    if (message.getSenderId().equals(chatHistory.getOtherUserId())) {
                        markAsRead(message.getSenderId());
                    }
                }
            }
        });
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