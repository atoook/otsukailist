package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.SyncGeneratedItemsRequest;
import com.atoook.otsukailist.dto.SyncGeneratedItemsResponse;
import com.atoook.otsukailist.service.GeneratedItemCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/generated-items")
public class GeneratedItemCommandController {

  private final GeneratedItemCommandService generatedItemCommandService;

  /**
   * Sync generated item candidates to a list.
   *
   * @param listId target list ID
   * @param req generated item candidates and generator-key scope
   * @return synced items with latest list revision
   */
  @PostMapping("/sync")
  public ResponseEntity<MutationResponse<SyncGeneratedItemsResponse>> syncGeneratedItems(
      @PathVariable("listId") UUID listId, @Valid @RequestBody SyncGeneratedItemsRequest req) {
    return ResponseEntity.ok(generatedItemCommandService.syncGeneratedItems(listId, req));
  }
}
