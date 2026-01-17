package com.atoook.otsukailist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item の作成リクエスト用DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequest {

    @NotBlank(message = "アイテム名は必須です")
    @Size(max = 200, message = "アイテム名は200文字以下にしてください")
    private String name;

    // 作成時に完了状態を指定可能（デフォルト: false）
    @Builder.Default
    private boolean completed = false;
}
