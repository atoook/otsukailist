package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.CreateItemListWithMembersRequest;
import com.atoook.otsukailist.dto.CreateItemListWithMembersResponse;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemListRequest;
import com.atoook.otsukailist.service.ListCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/list")
public class ItemListCommandController {
    private final ListCommandService listCommandService;

    @PostMapping
    public MutationResponse<CreateItemListWithMembersResponse> createItemList(
            @RequestBody CreateItemListWithMembersRequest req) {
        return listCommandService.createListWithMembers(req);
    }

    @PostMapping("/{listId}")
    public MutationResponse<ItemListResponse> rename(
            @PathVariable UUID listId,
            @Valid @RequestBody UpdateItemListRequest req) {
        return listCommandService.renameList(listId, req);
    }

}
