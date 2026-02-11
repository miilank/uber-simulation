package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.chat.ChatHistoryDTO;
import com.uberplus.backend.dto.chat.ChatMessageCreateDTO;
import com.uberplus.backend.dto.chat.ChatMessageDTO;
import com.uberplus.backend.dto.chat.ConversationPreviewDTO;
import com.uberplus.backend.model.ChatMessage;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.ChatMessageRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(ChatMessageCreateDTO request, User sender) {
        System.out.println("SENDING MESSAGE");
        System.out.println("Sender ID: " + sender.getId());
        System.out.println("Recipient ID: " + request.getRecipientId());

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (sender.getRole() != UserRole.ADMIN && recipient.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("You can only send messages to administrators");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setMessage(request.getMessage());
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageDTO dto = mapToDTO(saved);

        System.out.println("Message saved with ID: " + saved.getId());

        try {
            String destination = "/topic/messages/" + recipient.getId();

            System.out.println("Sending to destination: " + destination);
            System.out.println("DTO: " + dto);

            messagingTemplate.convertAndSend(destination, dto);

            System.out.println("Message sent successfully via WebSocket");
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryDTO getChatHistory(User user) {
        System.out.println("Getting chat history for user: " + user.getId() + " (" + user.getEmail() + ")");

        try {
            List<User> allUsers = userRepository.findAll();
            System.out.println("Total users in database: " + allUsers.size());

            long adminCount = allUsers.stream()
                    .filter(u -> u.getRole() == UserRole.ADMIN)
                    .count();
            System.out.println("Admin users found: " + adminCount);

            User admin = allUsers.stream()
                    .filter(u -> u.getRole() == UserRole.ADMIN)
                    .findFirst()
                    .orElseThrow(() -> {
                        System.err.println("NO ADMIN USER FOUND IN DATABASE!");
                        return new RuntimeException("No admin found");
                    });

            System.out.println("Found admin: " + admin.getId() + " (" + admin.getEmail() + ")");

            List<ChatMessage> messages = chatMessageRepository
                    .findChatHistoryBetweenUsers(user.getId(), admin.getId());

            System.out.println("Found " + messages.size() + " messages between users");

            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            ChatHistoryDTO history = new ChatHistoryDTO();
            history.setOtherUserId(admin.getId());
            history.setOtherUserName(admin.getFirstName() + " " + admin.getLastName());
            history.setMessages(messageDTOs);

            System.out.println("Returning chat history with " + messageDTOs.size() + " messages");
            return history;

        } catch (Exception e) {
            System.err.println("ERROR in getChatHistory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryDTO getChatHistoryWithUser(User admin, Integer userId) {
        System.out.println("Admin " + admin.getId() + " getting chat history with user: " + userId);

        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatMessage> messages = chatMessageRepository
                .findChatHistoryBetweenUsers(admin.getId(), userId);

        System.out.println("Found " + messages.size() + " messages");

        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        ChatHistoryDTO history = new ChatHistoryDTO();
        history.setOtherUserId(otherUser.getId());
        history.setOtherUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
        history.setMessages(messageDTOs);

        return history;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer senderId, Integer recipientId) {
        System.out.println("Marking messages as read: sender=" + senderId + ", recipient=" + recipientId);
        chatMessageRepository.markMessagesAsRead(recipientId, senderId);
    }

    private ChatMessageDTO mapToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setSenderRole(message.getSender().getRole());
        dto.setRecipientId(message.getRecipient().getId());
        dto.setMessage(message.getMessage());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationPreviewDTO> getConversations(User admin) {
        // Pronadji sve korisnike koji nisu admini
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .collect(Collectors.toList());

        List<ConversationPreviewDTO> conversations = new ArrayList<>();

        for (User user : users) {
            // Pronadji sve poruke izmedju admina i korisnika
            List<ChatMessage> messages = chatMessageRepository
                    .findChatHistoryBetweenUsers(admin.getId(), user.getId());

            if (!messages.isEmpty()) {
                // Posljednja poruka
                ChatMessage lastMessage = messages.get(messages.size() - 1);

                // Broj neprocitanih poruka (koje je korisnik poslao adminu)
                long unreadCount = messages.stream()
                        .filter(m -> m.getRecipient().getId().equals(admin.getId()) && !m.isRead())
                        .count();

                ConversationPreviewDTO preview = new ConversationPreviewDTO();
                preview.setUserId(user.getId());
                preview.setUserName(user.getFirstName() + " " + user.getLastName());
                preview.setUserEmail(user.getEmail());
                preview.setUserRole(user.getRole().toString());
                preview.setLastMessage(lastMessage.getMessage());
                preview.setLastMessageTime(lastMessage.getCreatedAt());
                preview.setUnreadCount((int) unreadCount);

                conversations.add(preview);
            }
        }

        // Sortiraj po vremenu posljednje poruke (najnovija prva)
        conversations.sort((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()));

        return conversations;
    }
}