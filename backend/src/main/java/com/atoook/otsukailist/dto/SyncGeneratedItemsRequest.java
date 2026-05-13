package com.atoook.otsukailist.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
public class SyncGeneratedItemsRequest {

  @Size(min = 1, max = 100, message = "生成ルールIDは1件以上100件以下で指定してください")
  @NotNull(message = "生成ルールID一覧は必須です")
  private List<String> generatorKeysInScope;

  @NotNull(message = "生成アイテム一覧は必須です")
  @Size(max = 100, message = "生成アイテムは100件以下で指定してください")
  private List<@Valid CreateItemRequest> items;
}
