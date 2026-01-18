package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Item List のレスポンス用DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class ItemListResponse {

    private UUID id;

    private String name;

    private long revision;

    private Instant createdAt;

    private Instant updatedAt;
}
