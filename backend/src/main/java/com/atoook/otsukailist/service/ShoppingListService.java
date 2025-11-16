package com.atoook.otsukailist.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.atoook.otsukailist.dto.CreateShoppingListRequest;
import com.atoook.otsukailist.dto.ShoppingListResponse;
import com.atoook.otsukailist.mapper.ShoppingListMapper;
import com.atoook.otsukailist.model.ShoppingList;
import com.atoook.otsukailist.repository.ShoppingListRepository;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    public ShoppingListService(ShoppingListRepository shoppingListRepository) {
        this.shoppingListRepository = shoppingListRepository;
    }

    /**
     * ID で Shopping List を取得
     */
    public Optional<ShoppingListResponse> getShoppingListById(UUID id) {
        return this.shoppingListRepository.findById(id)
                .map(ShoppingListMapper::toResponse);
    }

    /**
     * ID で Shopping List を取得（アイテム詳細含む）
     */
    public Optional<ShoppingListResponse> getShoppingListByIdWithItems(UUID id) {
        return this.shoppingListRepository.findById(id)
                .map(entity -> ShoppingListMapper.toResponse(entity, true));
    }

    /**
     * 新規 Shopping List 作成
     */
    public ShoppingListResponse createShoppingList(CreateShoppingListRequest request) {
        ShoppingList entity = ShoppingListMapper.toEntity(request);
        entity.setId(UUID.randomUUID());

        ShoppingList saved = this.shoppingListRepository.save(entity);
        return ShoppingListMapper.toResponse(saved);
    }

    /**
     * 名前なしで Shopping List 作成
     */
    public ShoppingListResponse createShoppingList() {
        CreateShoppingListRequest request = CreateShoppingListRequest.builder()
                .name("新しいリスト")
                .build();
        return createShoppingList(request);
    }

    /**
     * Shopping List 更新
     */
    public Optional<ShoppingListResponse> updateShoppingList(UUID id, CreateShoppingListRequest request) {
        Optional<ShoppingList> existingOpt = this.shoppingListRepository.findById(id);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        ShoppingList existing = existingOpt.get();
        ShoppingListMapper.updateEntity(existing, request);

        ShoppingList saved = this.shoppingListRepository.save(existing);
        return Optional.of(ShoppingListMapper.toResponse(saved));
    }

    public boolean deleteShoppingList(UUID id) {
        if (this.shoppingListRepository.existsById(id)) {
            this.shoppingListRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
