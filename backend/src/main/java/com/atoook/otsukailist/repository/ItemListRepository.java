package com.atoook.otsukailist.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atoook.otsukailist.model.ItemList;

@Repository
public interface ItemListRepository extends JpaRepository<ItemList, UUID> {
    // 基本的なCRUD操作は JpaRepository が自動提供
    // - findById(UUID id)
    // - findAll()
    // - save(ItemList entity)
    // - deleteById(UUID id)
    // - count()
    // - existsById(UUID id)

    // 必要に応じてカスタムクエリメソッドを追加
    // 例: List<ItemList> findByCreatedAtAfter(LocalDateTime date);
}
