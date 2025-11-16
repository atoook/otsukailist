package com.atoook.otsukailist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item の更新リクエスト用DTO
 * - チェック状態の変更
 * - 名前の変更
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequest {

    // 名前は任意更新（nullの場合は更新しない）
    private String name;

    // チェック状態（nullの場合は更新しない）
    private Boolean checked;
}
