package com.uberplus.backend.controller;

import com.uberplus.backend.dto.chat.ChatHistoryDTO;
import com.uberplus.backend.dto.chat.ChatMessageCreateDTO;
import com.uberplus.backend.dto.chat.ChatMessageDTO;
import com.uberplus.backend.dto.chat.ConversationPreviewDTO;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody ChatMessageCreateDTO request,
            Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessageDTO message = chatService.sendMessage(request, currentUser);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history")
    public ResponseEntity<ChatHistoryDTO> getChatHistory(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatHistoryDTO history = chatService.getChatHistory(currentUser);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ChatHistoryDTO> getChatHistoryWithUser(
            @PathVariable Integer userId,
            Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can access specific user chats");
        }

        ChatHistoryDTO history = chatService.getChatHistoryWithUser(currentUser, userId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/mark-read/{senderId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer senderId,
            Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatService.markMessagesAsRead(senderId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationPreviewDTO>> getConversations(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can access conversations list");
        }

        List<ConversationPreviewDTO> conversations = chatService.getConversations(currentUser);
        return ResponseEntity.ok(conversations);
    }
}