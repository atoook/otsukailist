package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.atoook.otsukailist.model.Item;

public interface ItemRepository extends JpaRepository<Item, UUID> {
  // 基本的なCRUD操作は JpaRepository が自動提供
  // ※ findAll() や条件なし検索は使用禁止（設計思想に反する）

  // 一覧表示用の並び順（未完了: 追加順の最新先頭 / 完了: 完了日時の新しい順）
  @Query("""
      SELECT i
      FROM Item i
      WHERE i.itemList.id = :itemListId
      ORDER BY
        i.completed ASC,
        CASE WHEN i.completed = false THEN i.createdAt ELSE NULL END DESC,
        CASE WHEN i.completed = true THEN i.completedAt ELSE NULL END DESC,
        i.id ASC
      """)
  List<Item> findByItemListIdOrderByDisplayRules(@Param("itemListId") UUID itemListId);

  // 特定のアイテムを取得
  Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

  // リスト内のアイテム存在チェック
  boolean existsByIdAndItemListId(UUID itemId, UUID itemListId);
}
