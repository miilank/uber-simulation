package com.example.mobileapp.features.shared.api.dto;

import java.util.List;

public class ChatHistoryDto {
    private Integer otherUserId;
    private String otherUserName;
    private List<ChatMessageDto> messages;

    public ChatHistoryDto() {}

    public Integer getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Integer otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}