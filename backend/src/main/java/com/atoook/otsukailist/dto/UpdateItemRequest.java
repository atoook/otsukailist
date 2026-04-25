package com.atoook.otsukailist.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Item の更新リクエスト用DTO - 完了状態の変更 - 名前の変更 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequest {

  // 名前は任意更新（nullの場合は更新しない）
  @Size(max = 100, message = "アイテム名は100文字以下にしてください")
  private String name;

  // 完了状態（nullの場合は更新しない）
  private Boolean completed;

  private UUID assignedMemberId;

  @JsonIgnore private boolean assignedMemberIdPresent;

  // completed=true のとき必須（未完了に戻すときは不要）
  private UUID completedByMemberId;

  @JsonSetter("assignedMemberId")
  public void setAssignedMemberId(UUID assignedMemberId) {
    this.assignedMemberId = assignedMemberId;
    this.assignedMemberIdPresent = true;
  }
}
