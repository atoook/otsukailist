package com.atoook.otsukailist.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item のレスポンス用DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private UUID id;

    private String name;

    private boolean isChecked;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Shopping List の ID も含める（必要に応じて）
    private UUID listId;
}
