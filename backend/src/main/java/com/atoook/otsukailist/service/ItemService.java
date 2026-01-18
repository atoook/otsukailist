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
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.ItemListRepository;

/**
 * Item のビジネスロジック
 * - セキュリティ：指定されたlistIdに属するアイテムのみ操作可能
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemListRepository itemListRepository;

    public ItemService(ItemRepository itemRepository, ItemListRepository itemListRepository) {
        this.itemRepository = itemRepository;
        this.itemListRepository = itemListRepository;
    }

    /**
     * 指定されたリストのアイテム一覧を取得
     */
    public List<ItemResponse> getItemsByListId(UUID listId) {
        return this.itemRepository.findByItemListId(listId).stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * アイテムを取得
     */
    public Optional<ItemResponse> getItemById(UUID listId, UUID itemId) {
        return this.itemRepository.findByIdAndItemListId(itemId, listId)
                .map(ItemMapper::toResponse);
    }

    /**
     * 新規アイテム作成
     */
    public Optional<ItemResponse> createItem(UUID listId, CreateItemRequest request) {
        // Item List の存在確認
        Optional<ItemList> itemListOpt = this.itemListRepository.findById(listId);
        if (itemListOpt.isEmpty()) {
            return Optional.empty();
        }

        ItemList itemList = itemListOpt.get();
        Item entity = ItemMapper.toEntity(request, itemList);

        Item saved = this.itemRepository.save(entity);
        return Optional.of(ItemMapper.toResponse(saved));
    }

    /**
     * アイテム更新
     */
    @Transactional
    public Optional<ItemResponse> updateItem(UUID listId, UUID itemId, UpdateItemRequest request) {
        // Item List の存在確認
        // Item の存在確認（listId によるスコープ制限）
        Optional<Item> existingOpt = this.itemRepository.findByIdAndItemListId(itemId, listId);
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
        // Item List の存在確認
        if (this.itemRepository.existsByIdAndItemListId(itemId, listId)) {
            this.itemRepository.deleteById(itemId);
            return true;
        }
        return false;
    }

    /**
     * アイテムの完了状態を切り替える
     */
    @Transactional
    public Optional<ItemResponse> toggleItemCheck(UUID listId, UUID itemId) {
        // Item List の存在確認
        Optional<Item> existingOpt = this.itemRepository.findByIdAndItemListId(itemId, listId);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Item existing = existingOpt.get();
        existing.setCompleted(!existing.isCompleted());

        Item saved = this.itemRepository.save(existing);
        return Optional.of(ItemMapper.toResponse(saved));
    }
}
