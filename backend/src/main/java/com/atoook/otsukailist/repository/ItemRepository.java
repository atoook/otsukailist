package com.atoook.otsukailist.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemPreparationType;

public interface ItemRepository extends JpaRepository<Item, UUID> {
  // 基本的なCRUD操作は JpaRepository が自動提供
  // ※ findAll() や条件なし検索は使用禁止（設計思想に反する）

  // 一覧表示用の並び順（未完了: 持参じゃないものを優先して追加順の最新先頭 / 完了: 完了日時の新しい順）
  @EntityGraph(attributePaths = "quantified")
  @Query(
      """
      SELECT i
      FROM Item i
      WHERE i.itemList.id = :itemListId
      ORDER BY
        i.completed ASC,
        CASE
          WHEN i.completed = false
            AND i.preparationType = :preparationType
          THEN 1
          ELSE 0
        END ASC,
        CASE WHEN i.completed = false THEN i.createdAt ELSE NULL END DESC,
        CASE WHEN i.completed = true AND i.completedAt IS NULL THEN 1 ELSE 0 END ASC,
        CASE WHEN i.completed = true THEN i.completedAt ELSE NULL END DESC,
        i.id ASC
      """)
  List<Item> findByItemListIdOrderByDisplayRules(
      @Param("itemListId") UUID itemListId,
      @Param("preparationType") ItemPreparationType preparationType);

  // 特定のアイテムを取得
  @EntityGraph(attributePaths = "quantified")
  Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

  // リスト内のアイテム存在チェック
  boolean existsByIdAndItemListId(UUID itemId, UUID itemListId);

  /** 複数リストのアイテム集計をまとめて取得する。 存在しないlistIdは結果に含まれない（削除済み判定に使う）。 */
  @Query(
      """
      SELECT i.itemList.id                                    AS listId,
             COUNT(i)                                         AS itemCount,
             SUM(CASE WHEN i.completed = true THEN 1 ELSE 0 END) AS completeCount,
             MAX(i.updatedAt)                                 AS lastItemActivityAt
      FROM Item i
      WHERE i.itemList.id IN :listIds
      GROUP BY i.itemList.id
      """)
  List<ItemSummaryProjection> summarizeByListIds(@Param("listIds") List<UUID> listIds);

  /** アイテム集計クエリの結果射影 */
  interface ItemSummaryProjection {
    UUID getListId();

    long getItemCount();

    long getCompleteCount();

    Instant getLastItemActivityAt();
  }
}
