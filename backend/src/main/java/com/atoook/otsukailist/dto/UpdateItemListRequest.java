package com.atoook.otsukailist.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ItemList の更新リクエスト用DTO
 * - 名前の変更
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemListRequest {

    @NotBlank
    @Size(max = 100, message = "リスト名は100文字以下にしてください")
    private String name;
}
