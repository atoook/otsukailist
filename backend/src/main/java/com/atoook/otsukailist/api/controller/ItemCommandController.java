package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.DeleteItemResponse;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.service.ItemCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/items")
public class ItemCommandController {

    private final ItemCommandService itemCommandService;

    @PostMapping
    public MutationResponse<ItemResponse> create(
            @PathVariable("listId") UUID listId,
            @Valid @RequestBody CreateItemRequest req) {
        return itemCommandService.createItem(listId, req);
    }

    @PatchMapping("/{itemId}")
    public MutationResponse<ItemResponse> update(
            @PathVariable("listId") UUID listId,
            @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody UpdateItemRequest req) {
        return itemCommandService.updateItem(listId, itemId, req);
    }

    @DeleteMapping("/{itemId}")
    public MutationResponse<DeleteItemResponse> delete(
            @PathVariable("listId") UUID listId,
            @PathVariable("itemId") UUID itemId) {
        return itemCommandService.deleteItem(listId, itemId);
    }
}
