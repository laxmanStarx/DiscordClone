package com.example.distributedchatservice.controller;

import com.example.distributedchatservice.domain.*;
import com.example.distributedchatservice.dto.ServerDtos.*;
import com.example.distributedchatservice.security.JwtAuthenticationFilter.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerRepository serverRepository;
    private final ServerMemberRepository memberRepository;
    private final ChannelRepository channelRepository;

    /**
     * Process 5: List all servers the caller belongs to (populates Discord left-rail).
     */
    @GetMapping
    public ResponseEntity<List<ServerSummary>> getUserServers(@AuthenticationPrincipal AuthenticatedUser user) {
        List<ServerMemberEntity> memberships = memberRepository.findByUserId(user.userId());
        List<ServerSummary> summaries = new ArrayList<>();

        for (ServerMemberEntity membership : memberships) {
            serverRepository.findById(membership.getServerId()).ifPresent(server ->
                    summaries.add(new ServerSummary(
                            server.getId(),
                            server.getName(),
                            server.getInviteCode(),
                            membership.getRole()
                    ))
            );
        }
        return ResponseEntity.ok(summaries);
    }

    /**
     * Process 5: Create a workspace, assign OWNER role, and create default "#general" channel.
     */
    @PostMapping
    public ResponseEntity<ServerEntity> createServer(
            @RequestBody CreateServerRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if (request.name() == null || request.name().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Generate short invite code
        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        ServerEntity server = ServerEntity.builder()
                .name(request.name().trim())
                .ownerId(user.userId())
                .inviteCode(inviteCode)
                .build();
        serverRepository.save(server);

        // Add creator as OWNER
        memberRepository.save(ServerMemberEntity.builder()
                .serverId(server.getId())
                .userId(user.userId())
                .role("OWNER")
                .build());

        // Default #general text room
        channelRepository.save(ChannelEntity.builder()
                .serverId(server.getId())
                .name("general")
                .type("TEXT")
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(server);
    }

    /**
     * Process 5: Join an existing server using an invite code.
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinServer(
            @RequestBody JoinServerRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ServerEntity server = serverRepository.findByInviteCode(request.inviteCode())
                .orElse(null);

        if (server == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid invite code");
        }

        if (memberRepository.existsByServerIdAndUserId(server.getId(), user.userId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already a member of this server");
        }

        memberRepository.save(ServerMemberEntity.builder()
                .serverId(server.getId())
                .userId(user.userId())
                .role("MEMBER")
                .build());

        return ResponseEntity.ok(server);
    }

    /**
     * Process 6: Retrieve channels in a server (accessible only to members).
     */
    @GetMapping("/{serverId}/channels")
    public ResponseEntity<?> getChannels(
            @PathVariable UUID serverId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if (!memberRepository.existsByServerIdAndUserId(serverId, user.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to server channels");
        }

        return ResponseEntity.ok(channelRepository.findByServerId(serverId));
    }

    /**
     * Process 6: Create a new channel inside a server.
     */
    @PostMapping("/{serverId}/channels")
    public ResponseEntity<?> createChannel(
            @PathVariable UUID serverId,
            @RequestBody CreateChannelRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if (!memberRepository.existsByServerIdAndUserId(serverId, user.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        ChannelEntity channel = ChannelEntity.builder()
                .serverId(serverId)
                .name(request.name().trim().toLowerCase().replace(" ", "-"))
                .type(request.type() != null ? request.type().toUpperCase() : "TEXT")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(channelRepository.save(channel));
    }
}