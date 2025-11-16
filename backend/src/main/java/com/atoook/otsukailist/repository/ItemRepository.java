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

    // 特定のショッピングリストのアイテムを取得（必須：リストIDでスコープ）
    List<Item> findByShoppingListId(UUID shoppingListId);

    // 特定のアイテムを取得
    Optional<Item> findByIdAndShoppingListId(UUID itemId, UUID shoppingListId);

    // 特定のショッピングリストでチェック済みのアイテムを取得
    List<Item> findByShoppingListIdAndIsChecked(UUID shoppingListId, boolean isChecked);

    // 特定のショッピングリストでチェック済みアイテムの数を取得
    long countByShoppingListIdAndIsChecked(UUID shoppingListId, boolean isChecked);

    // アイテム名で部分検索（必須：リストIDでスコープ）
    List<Item> findByShoppingListIdAndNameContaining(UUID shoppingListId, String name);

    // リスト内のアイテム存在チェック
    boolean existsByIdAndShoppingListId(UUID itemId, UUID shoppingListId);
}
