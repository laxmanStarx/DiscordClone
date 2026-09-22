package com.example.distributedchatservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ServerMemberRepository extends JpaRepository<ServerMemberEntity, UUID> {
    List<ServerMemberEntity> findByUserId(UUID userId);
    List<ServerMemberEntity> findByServerId(UUID serverId);
    boolean existsByServerIdAndUserId(UUID serverId, UUID userId);
}