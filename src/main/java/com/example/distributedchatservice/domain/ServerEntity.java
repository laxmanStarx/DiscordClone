package com.example.distributedchatservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a Discord Server (Guild / Workspace)
 */
@Entity
@Table(name = "servers", indexes = {
        @Index(name = "idx_servers_invite_code", columnList = "invite_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // The user who created this server and has OWNER permissions
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    // Unique short code used for shareable join links (e.g. "a9f1b2c3")
    @Column(name = "invite_code", unique = true, nullable = false)
    private String inviteCode;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}