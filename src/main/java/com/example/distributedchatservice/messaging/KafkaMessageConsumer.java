package com.example.distributedchatservice.messaging;

import com.example.distributedchatservice.domain.ChatMessageEntity;
import com.example.distributedchatservice.dto.ChatMessagePayload;
import com.example.distributedchatservice.domain.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final ChatMessageRepository repository;

    @KafkaListener(topics = "chat-messages", groupId = "chat-db-persister")
    public void consume(ChatMessagePayload message) {
        log.info("Persisting message from room {} sent by {}", message.roomId(), message.senderId());

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(message.messageId())
                .roomId(message.roomId())
                .senderId(message.senderId())
                .content(message.content())
                .timestamp(message.timestamp())
                .build();

        repository.save(entity);
    }
}