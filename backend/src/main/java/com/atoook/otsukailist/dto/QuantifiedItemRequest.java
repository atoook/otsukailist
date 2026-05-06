package com.atoook.otsukailist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuantifiedItemRequest {

  @NotNull(message = "数量は必須です")
  @PositiveOrZero(message = "数量は0以上にしてください")
  private Long quantity;

  @NotNull(message = "基準単位は必須です")
  private BaseUnit baseUnit;

  @NotNull(message = "作成元は必須です")
  private Origin origin;

  @NotNull(message = "再生成ポリシーは必須です")
  private RegenerationPolicy regenerationPolicy;

  @Size(max = 80, message = "生成ルールIDは80文字以下にしてください")
  private String generatorKey;
}
