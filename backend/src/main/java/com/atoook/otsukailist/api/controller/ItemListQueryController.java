package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.ItemListSnapshotResponse;
import com.atoook.otsukailist.service.ListQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists")
public class ItemListQueryController {
    private final ListQueryService listQueryService;

    /**
     * Get the latest snapshot of the specified item list.
     *
     * @param listId the ID of the item list
     * @return 200 snapshot of the item list
     */
    @GetMapping("/{listId}/snapshot")
    public ResponseEntity<ItemListSnapshotResponse> snapshot(
            @PathVariable("listId") UUID listId) {
        return ResponseEntity.ok(listQueryService.snapshot(listId));
    }

}
