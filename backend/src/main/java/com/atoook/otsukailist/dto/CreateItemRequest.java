package com.atoook.otsukailist.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.atoook.otsukailist.model.ItemCategory;
import com.atoook.otsukailist.model.ItemPreparationType;
import com.atoook.otsukailist.model.ItemType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Item の作成リクエスト用DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequest {

  @NotBlank(message = "アイテム名は必須です")
  @Size(max = 100, message = "アイテム名は100文字以下にしてください")
  private String name;

  // 作成時に完了状態を指定可能（デフォルト: false）
  @Builder.Default private boolean completed = false;

  @Builder.Default private ItemType itemType = ItemType.PLAIN;

  private ItemCategory category;

  private ItemPreparationType preparationType;

  private UUID assignedMemberId;

  @Valid private QuantifiedItemRequest quantified;
}
