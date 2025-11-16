package com.atoook.otsukailist.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atoook.otsukailist.model.ShoppingList;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {
    // 基本的なCRUD操作は JpaRepository が自動提供
    // - findById(UUID id)
    // - findAll()
    // - save(ShoppingList entity)
    // - deleteById(UUID id)
    // - count()
    // - existsById(UUID id)

    // 必要に応じてカスタムクエリメソッドを追加
    // 例: List<ShoppingList> findByCreatedAtAfter(LocalDateTime date);
}
