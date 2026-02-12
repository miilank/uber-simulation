package com.example.mobileapp.features.shared.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mobileapp.features.shared.api.dto.ChatMessageDto;
import com.example.mobileapp.features.shared.api.dto.NotificationDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "http://192.168.50.211:8080/ws/websocket";

    private StompClient stompClient;
    private final CompositeDisposable compositeDisposable;
    private MessageListener messageListener;
    private NotificationListener notificationListener;
    private final Gson gson;
    private Integer userId;
    private final SharedPreferences authPrefs;

    public interface MessageListener {
        void onMessageReceived(ChatMessageDto message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public interface NotificationListener {
        void onNotificationReceived(NotificationDto notification);
    }

    public WebSocketManager(Context context) {
        compositeDisposable = new CompositeDisposable();
        authPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);

        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

    public void connect(Integer userId, MessageListener listener) {
        this.userId = userId;
        this.messageListener = listener;

        String token = authPrefs.getString("jwt", null);

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);
        stompClient.withClientHeartbeat(4000).withServerHeartbeat(4000);

        Disposable lifecycleDisposable = stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            if (messageListener != null) {
                                messageListener.onConnected();
                            }
                            break;
                        case CLOSED:
                            Log.d(TAG, "Stomp connection closed");
                            if (messageListener != null) {
                                messageListener.onDisconnected();
                            }
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            if (messageListener != null) {
                                messageListener.onError(lifecycleEvent.getException().getMessage());
                            }
                            break;
                    }
                });
        compositeDisposable.add(lifecycleDisposable);

        Disposable messageDisposable = stompClient.topic("/topic/messages/" + userId)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received message: " + topicMessage.getPayload());
                    ChatMessageDto chatMessage = gson.fromJson(topicMessage.getPayload(), ChatMessageDto.class);
                    if (messageListener != null) {
                        messageListener.onMessageReceived(chatMessage);
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                    if (messageListener != null) {
                        messageListener.onError(throwable.getMessage());
                    }
                });
        compositeDisposable.add(messageDisposable);

        Disposable notificationDisposable = stompClient.topic("/topic/notifications/" + userId)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received notification: " + topicMessage.getPayload());
                    NotificationDto notification = gson.fromJson(topicMessage.getPayload(), NotificationDto.class);
                    if (notificationListener != null) {
                        notificationListener.onNotificationReceived(notification);
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe notifications", throwable);
                });
        compositeDisposable.add(notificationDisposable);

        List<StompHeader> headers = new ArrayList<>();
        if (token != null) {
            headers.add(new StompHeader("Authorization", "Bearer " + token));
        }
        stompClient.connect(headers);
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public boolean isConnected() {
        return stompClient != null && stompClient.isConnected();
    }
}