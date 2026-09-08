package com.example.distributedchatservice.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record ChatMessagePayload(
        UUID messageId,
        String roomId,
        String senderId,
        String content,
        Instant timestamp
) implements Serializable {}