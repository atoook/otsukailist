package com.atoook.otsukailist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shopping List の作成・更新リクエスト用DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShoppingListRequest {

    @NotBlank(message = "リスト名は必須です")
    @Size(max = 100, message = "リスト名は100文字以下にしてください")
    private String name;
}
