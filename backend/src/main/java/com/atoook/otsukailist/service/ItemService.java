package com.atoook.otsukailist.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ShoppingList;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.ShoppingListRepository;

/**
 * Item のビジネスロジック
 * - セキュリティ：指定されたlistIdに属するアイテムのみ操作可能
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ShoppingListRepository shoppingListRepository;

    public ItemService(ItemRepository itemRepository, ShoppingListRepository shoppingListRepository) {
        this.itemRepository = itemRepository;
        this.shoppingListRepository = shoppingListRepository;
    }

    /**
     * 指定されたリストのアイテム一覧を取得
     */
    public List<ItemResponse> getItemsByListId(UUID listId) {
        return this.itemRepository.findByShoppingListId(listId).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * アイテムを取得
     */
    public Optional<ItemResponse> getItemById(UUID listId, UUID itemId) {
        return this.itemRepository.findByIdAndShoppingListId(itemId, listId)
                .map(ItemMapper::toResponse);
    }

    /**
     * 新規アイテム作成
     */
    public Optional<ItemResponse> createItem(UUID listId, CreateItemRequest request) {
        // Shopping List の存在確認
        Optional<ShoppingList> shoppingListOpt = this.shoppingListRepository.findById(listId);
        if (shoppingListOpt.isEmpty()) {
            return Optional.empty();
        }

        ShoppingList shoppingList = shoppingListOpt.get();
        Item entity = ItemMapper.toEntity(request, shoppingList);
        entity.setId(UUID.randomUUID());

        Item saved = this.itemRepository.save(entity);
        return Optional.of(ItemMapper.toResponse(saved));
    }

    /**
     * アイテム更新
     */
    @Transactional
    public Optional<ItemResponse> updateItem(UUID listId, UUID itemId, UpdateItemRequest request) {
        // Shopping List の存在確認
        Optional<Item> existingOpt = this.itemRepository.findByIdAndShoppingListId(itemId, listId);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Item existing = existingOpt.get();
        ItemMapper.updateEntity(existing, request);

        Item saved = this.itemRepository.save(existing);
        return Optional.of(ItemMapper.toResponse(saved));
    }

    /**
     * アイテム削除
     */
    @Transactional
    public boolean deleteItem(UUID listId, UUID itemId) {
        // Shopping List の存在確認
        if (this.itemRepository.existsByIdAndShoppingListId(itemId, listId)) {
            this.itemRepository.deleteById(itemId);
            return true;
        }
        return false;
    }

    /**
     * アイテムのチェック状態を切り替える
     */
    @Transactional
    public Optional<ItemResponse> toggleItemCheck(UUID listId, UUID itemId) {
        // Shopping List の存在確認
        Optional<Item> existingOpt = this.itemRepository.findByIdAndShoppingListId(itemId, listId);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Item existing = existingOpt.get();
        existing.setChecked(!existing.isChecked());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        Item saved = this.itemRepository.save(existing);
        return Optional.of(ItemMapper.toResponse(saved));
    }
}
