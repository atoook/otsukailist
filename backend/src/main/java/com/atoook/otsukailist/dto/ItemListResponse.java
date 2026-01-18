package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item List のレスポンス用DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemListResponse {

    private UUID id;

    private String name;

    private long revision;

    private Instant createdAt;

    private Instant updatedAt;
}
