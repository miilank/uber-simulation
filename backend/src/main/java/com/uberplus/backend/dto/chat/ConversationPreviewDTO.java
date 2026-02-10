package com.uberplus.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationPreviewDTO {
    private Integer userId;
    private String userName;
    private String userEmail;
    private String userRole;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}