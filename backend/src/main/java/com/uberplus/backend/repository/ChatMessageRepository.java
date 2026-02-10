package com.uberplus.backend.repository;

import com.uberplus.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
            "(cm.sender.id = :userId1 AND cm.recipient.id = :userId2) OR " +
            "(cm.sender.id = :userId2 AND cm.recipient.id = :userId1) " +
            "ORDER BY cm.createdAt ASC")
    List<ChatMessage> findChatHistoryBetweenUsers(
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2
    );

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.recipient.id = :userId AND cm.read = false")
    Long countUnreadMessages(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.read = true WHERE cm.recipient.id = :recipientId AND cm.sender.id = :senderId")
    void markMessagesAsRead(@Param("recipientId") Integer recipientId, @Param("senderId") Integer senderId);
}