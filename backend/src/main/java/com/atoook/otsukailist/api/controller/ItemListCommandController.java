package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.CreateItemListWithMembersRequest;
import com.atoook.otsukailist.dto.CreateItemListWithMembersResponse;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemListRequest;
import com.atoook.otsukailist.service.ListCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists")
public class ItemListCommandController {
  private final ListCommandService listCommandService;

  @PostMapping
  public MutationResponse<CreateItemListWithMembersResponse> createItemList(
      @Valid @RequestBody CreateItemListWithMembersRequest req) {
    return listCommandService.createListWithMembers(req);
  }

  @PatchMapping("/{listId}")
  public MutationResponse<ItemListResponse> rename(
      @PathVariable("listId") UUID listId, @Valid @RequestBody UpdateItemListRequest req) {
    return listCommandService.renameList(listId, req);
  }
}
