package com.uberplus.backend.config;

import com.uberplus.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            System.out.println("WEBSOCKET MESSAGE");
            System.out.println("Command: " + accessor.getCommand());
            System.out.println("Session ID: " + accessor.getSessionId());
            System.out.println("User: " + accessor.getUser());

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                System.out.println("Authorization header present: " + (authHeader != null));

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        if (jwtService.isTokenValid(token)) {
                            Integer userId = jwtService.extractUserId(token);
                            System.out.println("Authenticated user ID: " + userId);
                            System.out.println("Token validated successfully");
                        }
                    } catch (Exception e) {
                        System.err.println("WebSocket auth failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                System.out.println("USER DISCONNECTED");
            }
        }

        return message;
    }
}