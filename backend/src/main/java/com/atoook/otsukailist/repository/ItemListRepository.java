package com.atoook.otsukailist.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.atoook.otsukailist.model.ItemList;

public interface ItemListRepository extends JpaRepository<ItemList, UUID> {
    // 基本的なCRUD操作は JpaRepository が自動提供
    // - findById(UUID id)
    // - findAll()
    // - save(ItemList entity)
    // - deleteById(UUID id)
    // - count()
    // - existsById(UUID id)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ItemList l set l.revision = l.revision + 1 where l.id = :listId")
    int incrementRevision(@Param("listId") UUID listId);

    @Query("select l.revision from ItemList l where l.id = :listId")
    Optional<Long> findRevision(@Param("listId") UUID listId);
}
