package com.atoook.otsukailist.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item の更新リクエスト用DTO
 * - 完了状態の変更
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

    // 完了状態（nullの場合は更新しない）
    private Boolean completed;

    // completed=true のとき必須（未完了に戻すときは不要）
    private UUID completedByMemberId;
}
