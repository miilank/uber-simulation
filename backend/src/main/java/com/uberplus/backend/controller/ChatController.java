package com.uberplus.backend.controller;

import com.uberplus.backend.dto.chat.ChatHistoryDTO;
import com.uberplus.backend.dto.chat.ChatMessageCreateDTO;
import com.uberplus.backend.dto.chat.ChatMessageDTO;
import com.uberplus.backend.repository.ChatMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatRepository;

    // POST /api/chat
    @PostMapping
    public ResponseEntity<ChatMessageDTO> sendMessage(@Valid @RequestBody ChatMessageCreateDTO request) {
        return ResponseEntity.ok(new ChatMessageDTO());
    }

    // GET /api/chat/history
    @GetMapping("/history")
    public ResponseEntity<ChatHistoryDTO> getChatHistory() {
        return ResponseEntity.ok(new ChatHistoryDTO());
    }
}
