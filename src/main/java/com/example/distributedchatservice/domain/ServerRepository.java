package com.example.distributedchatservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ServerRepository extends JpaRepository<ServerEntity, UUID> {
    Optional<ServerEntity> findByInviteCode(String inviteCode);
}