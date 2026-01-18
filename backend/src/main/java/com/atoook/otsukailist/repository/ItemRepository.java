package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atoook.otsukailist.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    // 基本的なCRUD操作は JpaRepository が自動提供
    // ※ findAll() や条件なし検索は使用禁止（設計思想に反する）

    // 特定のアイテムリストのアイテムを取得（必須：リストIDでスコープ）
    List<Item> findByItemListId(UUID itemListId);

    // 特定のアイテムを取得
    Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

    // 特定のアイテムリストで完了済みのアイテムを取得
    List<Item> findByItemListIdAndIsCompleted(UUID itemListId, boolean isCompleted);

    // 特定のアイテムリストで完了済みアイテムの数を取得
    long countByItemListIdAndIsCompleted(UUID itemListId, boolean isCompleted);

    // アイテム名で部分検索（必須：リストIDでスコープ）
    List<Item> findByItemListIdAndNameContaining(UUID itemListId, String name);

    // リスト内のアイテム存在チェック
    boolean existsByIdAndItemListId(UUID itemId, UUID itemListId);
}
