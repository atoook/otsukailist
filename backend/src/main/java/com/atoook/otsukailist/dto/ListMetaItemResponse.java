package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 複数リストのメタ情報バッチ取得レスポンスの1件分 */
@Getter
@AllArgsConstructor
@Builder
public class ListMetaItemResponse {

  private UUID listId;

  private String name;

  private long itemCount;

  private long completeCount;

  /** アイテムの最終更新日時。アイテムが0件の場合は null。 */
  private Instant lastItemActivityAt;
}
