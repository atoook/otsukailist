package com.atoook.otsukailist.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.ItemListSnapshotResponse;
import com.atoook.otsukailist.dto.ListMetaItemResponse;
import com.atoook.otsukailist.service.ListQueryService;

import lombok.RequiredArgsConstructor;

@Validated
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
  public ResponseEntity<ItemListSnapshotResponse> snapshot(@PathVariable("listId") UUID listId) {
    return ResponseEntity.ok(listQueryService.snapshot(listId));
  }

  /**
   * Get metadata (name, item counts, last activity) for a batch of lists.
   *
   * <p>List IDs that no longer exist are silently omitted from the response.
   *
   * @param listIds list of listIds to query (max 10)
   * @return 200 metadata for each existing list
   */
  @GetMapping("/meta")
  public ResponseEntity<List<ListMetaItemResponse>> listsMeta(
      @RequestParam @NotEmpty @Size(max = 10) List<UUID> listIds) {
    return ResponseEntity.ok(listQueryService.getListsMeta(listIds));
  }
}
