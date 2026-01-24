package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atoook.otsukailist.model.Item;

public interface ItemRepository extends JpaRepository<Item, UUID> {
  // 基本的なCRUD操作は JpaRepository が自動提供
  // ※ findAll() や条件なし検索は使用禁止（設計思想に反する）

  // 特定のアイテムリストのアイテムを取得（必須：リストIDでスコープ）
  List<Item> findByItemListId(UUID itemListId);

  // 特定のアイテムを取得
  Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

  // リスト内のアイテム存在チェック
  boolean existsByIdAndItemListId(UUID itemId, UUID itemListId);
}
