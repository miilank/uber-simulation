package com.uberplus.backend.dto.chat;

import com.uberplus.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private UserRole senderRole;
    private Integer recipientId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
