package com.atoook.otsukailist.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.CreateItemListRequest;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.mapper.ItemListMapper;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemListRepository;

@Service
public class ItemListService {

    private final ItemListRepository itemListRepository;

    public ItemListService(ItemListRepository itemListRepository) {
        this.itemListRepository = itemListRepository;
    }

    /**
     * ID で Item List を取得
     */
    public Optional<ItemListResponse> getItemListById(UUID id) {
        return this.itemListRepository.findById(id)
                .map(ItemListMapper::toResponse);
    }

    /**
     * ID で Item List を取得（アイテム詳細含む）
     */
    public Optional<ItemListResponse> getItemListByIdWithItems(UUID id) {
        return this.itemListRepository.findById(id)
                .map(entity -> ItemListMapper.toResponse(entity, true));
    }

    /**
     * 新規 Item List 作成
     */
    public ItemListResponse createItemList(CreateItemListRequest request) {
        ItemList entity = ItemListMapper.toEntity(request);

        ItemList saved = this.itemListRepository.save(entity);
        return ItemListMapper.toResponse(saved);
    }

    /**
     * 名前なしで Item List 作成
     */
    public ItemListResponse createItemList() {
        CreateItemListRequest request = CreateItemListRequest.builder()
                .name("新しいリスト")
                .build();
        return createItemList(request);
    }

    /**
     * Item List 更新
     */
    @Transactional
    public Optional<ItemListResponse> updateItemList(UUID id, CreateItemListRequest request) {
        Optional<ItemList> existingOpt = this.itemListRepository.findById(id);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        ItemList existing = existingOpt.get();
        ItemListMapper.updateEntity(existing, request);

        ItemList saved = this.itemListRepository.save(existing);
        return Optional.of(ItemListMapper.toResponse(saved));
    }

    /**
     * Item List を削除
     */
    @Transactional
    public boolean deleteItemList(UUID id) {
        boolean exists = this.itemListRepository.existsById(id);
        if (exists) {
            this.itemListRepository.deleteById(id);
        }
        return exists;
    }
}
