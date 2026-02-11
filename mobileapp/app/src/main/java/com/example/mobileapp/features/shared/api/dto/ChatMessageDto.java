package com.example.mobileapp.features.shared.api.dto;

import com.example.mobileapp.features.shared.models.enums.UserRole;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private UserRole senderRole;
    private Integer recipientId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public ChatMessageDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public UserRole getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(UserRole senderRole) {
        this.senderRole = senderRole;
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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}