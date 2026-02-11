package com.example.mobileapp.features.shared.api.dto;

public class ChatMessageCreateDto {
    private Integer recipientId;
    private String message;

    public ChatMessageCreateDto() {}

    public ChatMessageCreateDto(Integer recipientId, String message) {
        this.recipientId = recipientId;
        this.message = message;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}