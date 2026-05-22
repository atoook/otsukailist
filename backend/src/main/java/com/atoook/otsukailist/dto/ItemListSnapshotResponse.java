package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Item List のSnapshotレスポンス用DTO * <MEMO> * このDTOは Service で * listRepo.findById *
 * memberRepo.findByItemListId * itemRepo.findByItemListId * で集めて作る（Mapperは個別に
 * ItemMapper/MemberMapper を使う） * → N+1を踏まない。
 */
@Getter
@AllArgsConstructor
@Builder
public class ItemListSnapshotResponse {

  private UUID listId;
  private String name;

  /** Socket差分の基準（リスト単位） */
  private long revision;

  /** リスト名など list metadata の競合制御用 version */
  private long version;

  /** UI表示用 */
  private int itemCount;

  /** 任意：クライアントの基準時刻 */
  private Instant serverTime;

  /** アイテムの最終更新日時（全アイテムの updatedAt の最大値）。アイテムが0件の場合は null。 */
  private Instant lastItemActivityAt;

  @Builder.Default private List<MemberResponse> members = List.of();
  @Builder.Default private List<ItemResponse> items = List.of();
}
