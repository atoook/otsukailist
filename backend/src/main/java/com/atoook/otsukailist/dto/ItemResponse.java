package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Item のレスポンス用DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private UUID id;

    private String name;

    private boolean completed;

    private UUID completedByMemberId;

    private Instant completedAt;

    private Instant createdAt;

    private Instant updatedAt;
}
