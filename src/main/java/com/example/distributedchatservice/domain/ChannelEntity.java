package com.example.distributedchatservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "channels", indexes = {
        @Index(name = "idx_channels_server_id", columnList = "server_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "server_id", nullable = false)
    private UUID serverId;

    @Column(nullable = false)
    private String name; // e.g. "general"

    @Column(nullable = false)
    private String type; // "TEXT" or "VOICE"
}