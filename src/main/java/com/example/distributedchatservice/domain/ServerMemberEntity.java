package com.example.distributedchatservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "server_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_server_user", columnNames = {"server_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "server_id", nullable = false)
    private UUID serverId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Role enum string: "OWNER", "ADMIN", "MEMBER"
    @Column(nullable = false)
    private String role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    protected void onJoin() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }
}