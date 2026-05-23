package com.atoook.otsukailist.api.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.atoook.otsukailist.api.HttpPreconditions;
import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.DeleteItemResponse;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.MarkItemCompletedRequest;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.service.ItemCommandService;

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
   * @param req creation payload
   * @return 201 created item response
   */
  @PostMapping
  public ResponseEntity<MutationResponse<ItemResponse>> create(
      @PathVariable("listId") UUID listId, @Valid @RequestBody CreateItemRequest req) {
    MutationResponse<ItemResponse> payload = itemCommandService.createItem(listId, req);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{itemId}")
            .buildAndExpand(payload.getData().getId())
            .toUri();
    return ResponseEntity.created(location).body(payload);
  }

  /**
   * Updates an existing item.
   *
   * @param listId parent list identifier
   * @param itemId item identifier
   * @param req update payload
   * @return 200 updated item response
   */
  @PatchMapping("/{itemId}")
  public ResponseEntity<MutationResponse<ItemResponse>> update(
      @PathVariable("listId") UUID listId,
      @PathVariable("itemId") UUID itemId,
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @Valid @RequestBody UpdateItemRequest req) {
    long expectedVersion = HttpPreconditions.requireIfMatchVersion(ifMatch);
    return ResponseEntity.ok(itemCommandService.updateItem(listId, itemId, req, expectedVersion));
  }

  /**
   * Marks an item as completed.
   *
   * @param listId parent list identifier
   * @param itemId item identifier
   * @param ifMatch expected item version
   * @param req completion request payload
   * @return 200 updated item response
   */
  @PatchMapping("/{itemId}/mark-completed")
  public ResponseEntity<MutationResponse<ItemResponse>> markCompleted(
      @PathVariable("listId") UUID listId,
      @PathVariable("itemId") UUID itemId,
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @Valid @RequestBody MarkItemCompletedRequest req) {
    long expectedVersion = HttpPreconditions.requireIfMatchVersion(ifMatch);
    return ResponseEntity.ok(
        itemCommandService.markCompleted(listId, itemId, req, expectedVersion));
  }

  /**
   * Marks an item as incomplete.
   *
   * @param listId parent list identifier
   * @param itemId item identifier
   * @param ifMatch expected item version
   * @return 200 updated item response
   */
  @PatchMapping("/{itemId}/mark-incomplete")
  public ResponseEntity<MutationResponse<ItemResponse>> markIncomplete(
      @PathVariable("listId") UUID listId,
      @PathVariable("itemId") UUID itemId,
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
    long expectedVersion = HttpPreconditions.requireIfMatchVersion(ifMatch);
    return ResponseEntity.ok(itemCommandService.markIncomplete(listId, itemId, expectedVersion));
  }

  /**
   * Deletes the specified item.
   *
   * @param listId parent list identifier
   * @param itemId item identifier
   * @return 200 deletion response with mutation payload
   */
  @DeleteMapping("/{itemId}")
  public ResponseEntity<MutationResponse<DeleteItemResponse>> delete(
      @PathVariable("listId") UUID listId,
      @PathVariable("itemId") UUID itemId,
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
    long expectedVersion = HttpPreconditions.requireIfMatchVersion(ifMatch);
    return ResponseEntity.ok(itemCommandService.deleteItem(listId, itemId, expectedVersion));
  }
}
