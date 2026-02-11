package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.ChatHistoryDto;
import com.example.mobileapp.features.shared.api.dto.ChatMessageCreateDto;
import com.example.mobileapp.features.shared.api.dto.ChatMessageDto;
import com.example.mobileapp.features.shared.api.dto.ConversationPreviewDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApi {

    @POST("api/chat")
    Call<ChatMessageDto> sendMessage(@Body ChatMessageCreateDto request);

    @GET("api/chat/history")
    Call<ChatHistoryDto> getChatHistory();

    @GET("api/chat/history/{userId}")
    Call<ChatHistoryDto> getChatHistoryWithUser(@Path("userId") Integer userId);

    @POST("api/chat/mark-read/{senderId}")
    Call<Void> markAsRead(@Path("senderId") Integer senderId);

    @GET("api/chat/conversations")
    Call<List<ConversationPreviewDto>> getConversations();
}