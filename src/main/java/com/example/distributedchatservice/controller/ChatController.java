package com.example.distributedchatservice.controller;

import com.example.distributedchatservice.dto.ChatMessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, ChatMessagePayload> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessagePayload incoming) throws Exception {
        ChatMessagePayload payload = new ChatMessagePayload(
                UUID.randomUUID(),
                incoming.roomId(),
                incoming.senderId(),
                incoming.content(),
                Instant.now()
        );

        // 1. Redis: Ephemeral fan-out across all active application pods
        String redisTopic = "room:" + payload.roomId();
        redisTemplate.convertAndSend(redisTopic, objectMapper.writeValueAsString(payload));

        // 2. Kafka: Durable write stream, strictly ordered by roomId partition key
        kafkaTemplate.send("chat-messages", payload.roomId(), payload);
    }
}