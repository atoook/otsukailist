package com.atoook.otsukailist.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.ItemListSnapshotResponse;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.mapper.MemberMapper;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.service.message.ErrorMessages;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListQueryService {

    private final ItemListRepository itemListRepo;
    private final MemberRepository memberRepo;
    private final ItemRepository itemRepo;

    @Transactional(readOnly = true)
    public ItemListSnapshotResponse snapshot(UUID listId) {
        ItemList list = itemListRepo.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));

        var members = memberRepo.findByItemListId(listId).stream()
                .map(MemberMapper::toResponse)
                .toList();

        var itemEntities = itemRepo.findByItemListId(listId);
        var items = itemEntities.stream()
                .map(ItemMapper::toResponse)
                .toList();

        return ItemListSnapshotResponse.builder()
                .listId(list.getId())
                .name(list.getName())
                .revision(list.getRevision())
                .itemCount(itemEntities.size())
                .serverTime(Instant.now())
                .members(members)
                .items(items)
                .build();
    }
}
