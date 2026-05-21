package com.atoook.otsukailist.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.MarkItemCompletedRequest;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.service.message.ErrorMessages;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCompletionService {

  private final ItemRepository itemRepo;
  private final ItemListRepository itemListRepo;
  private final MemberRepository memberRepo;
  private final ListRevisionService listRevisionService;

  private static final String MSG_MEMBER_NOT_IN_LIST = "指定された完了者はリストのメンバーではありません";
  private static final String MSG_COMPLETED_BY_NOT_SPECIFIED = "完了者が未指定です";

  /** Marks an item as completed without deriving the target state from the client cache. */
  @Transactional
  public MutationResponse<ItemResponse> markCompleted(
      UUID listId, UUID itemId, MarkItemCompletedRequest req) {
    Item item = findItemInList(listId, itemId);
    if (item.isCompleted()) {
      return currentMutationResponse(listId, item);
    }

    UUID completedByMemberId = req == null ? null : req.getCompletedByMemberId();
    validateCompletedByMember(listId, completedByMemberId);
    item.setCompleted(true);
    item.setCompletedByMemberId(completedByMemberId);
    item.setCompletedAt(Instant.now());
    return saveAndIncrementRevision(listId, item);
  }

  /** Marks an item as incomplete without deriving the target state from the client cache. */
  @Transactional
  public MutationResponse<ItemResponse> markIncomplete(UUID listId, UUID itemId) {
    Item item = findItemInList(listId, itemId);
    if (!item.isCompleted()) {
      return currentMutationResponse(listId, item);
    }

    item.setCompleted(false);
    item.setCompletedByMemberId(null);
    item.setCompletedAt(null);
    return saveAndIncrementRevision(listId, item);
  }

  private Item findItemInList(UUID listId, UUID itemId) {
    return itemRepo
        .findByIdAndItemListIdForUpdate(itemId, listId)
        .orElseThrow(
            () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "アイテム")));
  }

  private void validateCompletedByMember(UUID listId, UUID completedByMemberId) {
    if (completedByMemberId == null) {
      throw new BadRequestException(MSG_COMPLETED_BY_NOT_SPECIFIED);
    }
    boolean exists = memberRepo.existsByIdAndItemListId(completedByMemberId, listId);
    if (!exists) {
      throw new BadRequestException(MSG_MEMBER_NOT_IN_LIST);
    }
  }

  private MutationResponse<ItemResponse> saveAndIncrementRevision(UUID listId, Item item) {
    Item saved = itemRepo.save(item);
    long revision = listRevisionService.incrementAndGet(listId);
    return MutationResponse.<ItemResponse>builder()
        .revision(revision)
        .data(ItemMapper.toResponse(saved))
        .changed(true)
        .build();
  }

  private MutationResponse<ItemResponse> currentMutationResponse(UUID listId, Item item) {
    long revision =
        itemListRepo
            .findRevision(listId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));
    return MutationResponse.<ItemResponse>builder()
        .revision(revision)
        .data(ItemMapper.toResponse(item))
        .changed(false)
        .build();
  }
}
