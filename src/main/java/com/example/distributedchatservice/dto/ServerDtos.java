package com.example.distributedchatservice.dto;

import java.util.UUID;

public class ServerDtos {

    // Process 5: Create a new server
    public record CreateServerRequest(String name) {}

    // Process 5: Join an existing server via invite code
    public record JoinServerRequest(String inviteCode) {}

    // Process 6: Create a new channel inside a server
    public record CreateChannelRequest(String name, String type) {}

    // DTO for rendering servers on the left navigation rail
    public record ServerSummary(
            UUID id,
            String name,
            String inviteCode,
            String role
    ) {}
}