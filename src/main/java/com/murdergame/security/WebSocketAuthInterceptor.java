package com.murdergame.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        System.out.println("=== STOMP COMMAND: " + accessor.getCommand() + " ===");

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            System.out.println("=== TOKEN HEADER: " + token + " ===");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Claims claims = jwtService.parse(token);
                    accessor.setUser(() -> claims.getSubject());

                    // userId ve teamId'yi session'a kaydet
                    Long userId = claims.get("userId", Long.class);
                    String role = claims.get("role", String.class);
                    System.out.println("=== WS CONNECT userId: " + userId + " ===");

                    if (userId != null) {
                        accessor.getSessionAttributes().put("userId", userId);
                    }
                    if (role != null) {
                        accessor.getSessionAttributes().put("role", role);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Invalid token: " + e.getMessage());
                }
            }
        }

        return message;
    }
}