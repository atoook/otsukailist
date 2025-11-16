package com.atoook.otsukailist.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shopping List のレスポンス用DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListResponse {

    private UUID id;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // アイテム数
    private int itemCount;

    // アイテムの詳細（必要に応じて含める）
    private List<ItemResponse> items;
}
