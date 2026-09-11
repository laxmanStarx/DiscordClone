package com.example.distributedchatservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServerRepository extends JpaRepository<ServerEntity, UUID> {
    // Look up a server when someone clicks an invite link or enters a code
    Optional<ServerEntity> findByInviteCode(String inviteCode);
}