package com.example.distributedchatservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<ChannelEntity,UUID> {

    List<ChannelEntity> findByServerId(UUID serverId);
}
