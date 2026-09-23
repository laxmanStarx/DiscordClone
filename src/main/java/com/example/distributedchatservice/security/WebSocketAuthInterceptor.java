package com.example.distributedchatservice.security;

import com.example.distributedchatservice.domain.ChannelEntity;
import com.example.distributedchatservice.domain.ChannelRepository;
import com.example.distributedchatservice.domain.ServerMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final ChannelRepository channelRepository;
    private final ServerMemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 1. Authenticate when the client sends CONNECT frame
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    UUID userId = jwtService.extractUserId(token);
                    String username = jwtService.extractUsername(token);

                    Principal principal = new UsernamePasswordAuthenticationToken(
                            new JwtAuthenticationFilter.AuthenticatedUser(userId, username),
                            null,
                            Collections.emptyList()
                    );
                    accessor.setUser(principal);
                    log.info("STOMP Authenticated user: {} ({})", username, userId);
                } catch (Exception e) {
                    log.error("Invalid JWT token on STOMP CONNECT: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid token");
                }
            } else {
                log.warn("STOMP CONNECT attempted without Bearer token");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        // 2. Authorize channel membership when client sends SUBSCRIBE frame
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination(); // e.g. "/topic/channel.{channelId}"
            Principal principal = accessor.getUser();

            if (principal instanceof UsernamePasswordAuthenticationToken auth && destination != null) {
                var user = (JwtAuthenticationFilter.AuthenticatedUser) auth.getPrincipal();

                if (destination.startsWith("/topic/channel.")) {
                    String channelIdStr = destination.replace("/topic/channel.", "");
                    try {
                        UUID channelId = UUID.fromString(channelIdStr);
                        ChannelEntity channelEntity = channelRepository.findById(channelId).orElse(null);

                        if (channelEntity != null) {
                            boolean isMember = memberRepository.existsByServerIdAndUserId(channelEntity.getServerId(), user.userId());
                            if (!isMember) {
                                log.warn("User {} denied subscription to channel {} (not a server member)", user.username(), channelId);
                                throw new SecurityException("Access denied to channel");
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Not a UUID format (e.g. general test topic) - pass through
                    }
                }
            }
        }

        return message;
    }
}