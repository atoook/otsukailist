package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.CreateItemRequest;
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

    /**
     * Creates a new item under the given list.
     *
     * @param listId list identifier
     * @param req    creation payload
     * @return 201 created item response
     */
    @PostMapping
    public ResponseEntity<MutationResponse<ItemResponse>> create(
            @PathVariable("listId") UUID listId,
            @Valid @RequestBody CreateItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemCommandService.createItem(listId, req));
    }

    /**
     * Updates an existing item.
     *
     * @param listId parent list identifier
     * @param itemId item identifier
     * @param req    update payload
     * @return 200 updated item response
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<MutationResponse<ItemResponse>> update(
            @PathVariable("listId") UUID listId,
            @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody UpdateItemRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(itemCommandService.updateItem(listId, itemId, req));
    }

    /**
     * Deletes the specified item.
     *
     * @param listId parent list identifier
     * @param itemId item identifier
     * @return 204 deletion response
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(
            @PathVariable("listId") UUID listId,
            @PathVariable("itemId") UUID itemId) {
        itemCommandService.deleteItem(listId, itemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
