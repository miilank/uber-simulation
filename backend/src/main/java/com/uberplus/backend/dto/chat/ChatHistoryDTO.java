package com.uberplus.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryDTO {
    private Integer otherUserId;
    private String otherUserName;
    private List<ChatMessageDTO> messages;
}
