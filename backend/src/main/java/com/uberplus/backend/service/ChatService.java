package com.uberplus.backend.service;

import com.uberplus.backend.dto.chat.ChatHistoryDTO;
import com.uberplus.backend.dto.chat.ChatMessageCreateDTO;
import com.uberplus.backend.dto.chat.ChatMessageDTO;
import com.uberplus.backend.dto.chat.ConversationPreviewDTO;
import com.uberplus.backend.model.User;

import java.util.List;

public interface ChatService {
    ChatMessageDTO sendMessage(ChatMessageCreateDTO request, User sender);
    ChatHistoryDTO getChatHistory(User user);
    ChatHistoryDTO getChatHistoryWithUser(User admin, Integer userId);
    void markMessagesAsRead(Integer senderId, Integer recipientId);
    List<ConversationPreviewDTO> getConversations(User admin);
}