package com.example.distributedchatservice.messaging;

import com.example.distributedchatservice.dto.ChatMessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessagePayload payload = objectMapper.readValue(message.getBody(), ChatMessagePayload.class);
            // Broadcast directly to local WebSocket clients connected to this instance
            messagingTemplate.convertAndSend("/topic/room." + payload.roomId(), payload);
        } catch (Exception e) {
            log.error("Failed to deserialize and route Redis message", e);
        }
    }
}