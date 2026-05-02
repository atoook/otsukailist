package com.atoook.otsukailist.service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.DeleteItemResponse;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.generation.BBQGenerationRules;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemCategory;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ItemQuantified;
import com.atoook.otsukailist.model.ItemType;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.MemberRepository;
import com.atoook.otsukailist.service.message.ErrorMessages;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCommandService {

  private final ItemRepository itemRepo;
  private final ItemListRepository itemListRepo;
  private final MemberRepository memberRepo;

  private final ListRevisionService listRevisionService;

  private static final String MSG_MEMBER_NOT_IN_LIST = "指定された完了者はリストのメンバーではありません";
  private static final String MSG_ASSIGNED_MEMBER_NOT_IN_LIST = "指定された担当者はリストのメンバーではありません";
  private static final String MSG_COMPLETED_BY_NOT_SPECIFIED = "完了者が未指定です";
  private static final String MSG_QUANTIFIED_REQUIRED = "数量付きアイテム情報が未指定です";
  private static final String MSG_QUANTIFIED_NOT_ALLOWED = "通常アイテムに数量付きアイテム情報は指定できません";
  private static final String MSG_ITEM_NOT_QUANTIFIED = "数量付きアイテムではありません";
  private static final String MSG_GENERATOR_KEY_REQUIRED = "自動生成アイテムには生成ルールIDが必須です";

  /**
   * Item追加（listIdスコープ） - 完了状態を作成時に許可するなら completedByMemberId もDTOに追加するのが整合的 - ミニマムなら「作成時は未完了固定」を推奨
   */
  @Transactional
  public MutationResponse<ItemResponse> createItem(UUID listId, CreateItemRequest req) {
    // list存在確認
    ItemList list =
        itemListRepo
            .findById(listId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));

    // Entity作成（ミニマム：作成時は未完了固定）
    Item item = new Item();
    ItemType itemType = req.getItemType() == null ? ItemType.PLAIN : req.getItemType();
    validateQuantifiedCreateRequest(itemType, req);

    item.setName(resolveItemName(req, itemType));
    item.setItemType(itemType);
    item.setCategory(resolveCategory(req.getCategory(), req.getQuantified()));
    item.setCompleted(false);
    item.setAssignedMemberId(null);
    item.setCompletedByMemberId(null);
    item.setCompletedAt(null);
    item.setItemList(list);
    if (itemType == ItemType.QUANTIFIED) {
      ItemQuantified quantified = ItemMapper.toQuantifiedEntity(req.getQuantified());
      item.setQuantified(quantified);
    }

    Item saved = itemRepo.save(item);

    long revision = listRevisionService.incrementAndGet(listId);

    return MutationResponse.<ItemResponse>builder()
        .revision(revision)
        .data(ItemMapper.toResponse(saved))
        .build();
  }

  /** Item更新（rename / setCompleted） */
  @Transactional
  public MutationResponse<ItemResponse> updateItem(
      UUID listId, UUID itemId, UpdateItemRequest req) {
    Item item =
        itemRepo
            .findByIdAndItemListId(itemId, listId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "アイテム")));

    updateNameAndQuantified(item, req);
    updateAssignedMember(listId, item, req);
    updateCompletion(listId, item, req);

    Item saved = itemRepo.save(item);

    long revision = listRevisionService.incrementAndGet(listId);

    return MutationResponse.<ItemResponse>builder()
        .revision(revision)
        .data(ItemMapper.toResponse(saved))
        .build();
  }

  /** Item削除（listIdスコープ） */
  @Transactional
  public MutationResponse<DeleteItemResponse> deleteItem(UUID listId, UUID itemId) {
    // listIdスコープで存在確認
    Item item =
        itemRepo
            .findByIdAndItemListId(itemId, listId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "アイテム")));

    itemRepo.delete(item);

    long revision = listRevisionService.incrementAndGet(listId);

    return MutationResponse.<DeleteItemResponse>builder()
        .revision(revision)
        .data(DeleteItemResponse.builder().deletedItemId(itemId).build())
        .build();
  }

  private static void validateQuantifiedCreateRequest(ItemType itemType, CreateItemRequest req) {
    if (itemType == ItemType.QUANTIFIED && req.getQuantified() == null) {
      throw new BadRequestException(MSG_QUANTIFIED_REQUIRED);
    }
    if (itemType == ItemType.PLAIN && req.getQuantified() != null) {
      throw new BadRequestException(MSG_QUANTIFIED_NOT_ALLOWED);
    }
    if (req.getQuantified() != null) {
      validateGeneratorKey(req.getQuantified());
    }
  }

  private static void updateNameAndQuantified(Item item, UpdateItemRequest req) {
    boolean directNameEdited = req.getName() != null;
    boolean revertingToPlain =
        item.getItemType() == ItemType.QUANTIFIED && req.getItemType() == ItemType.PLAIN;
    boolean quantifiedDetailsEdited = req.getQuantified() != null;

    ItemMapper.updateEntity(item, req);
    if (revertingToPlain) {
      revertToPlain(item);
      return;
    }
    if (directNameEdited && !quantifiedDetailsEdited) {
      syncQuantifiedNameFromItemName(item);
      lockQuantifiedItem(item);
    }

    updateQuantifiedDetails(item, req);
  }

  private static void updateQuantifiedDetails(Item item, UpdateItemRequest req) {
    if (req.getQuantified() == null) {
      return;
    }
    validateGeneratorKey(req.getQuantified());
    if (item.getItemType() == ItemType.PLAIN && req.getItemType() == ItemType.QUANTIFIED) {
      item.setItemType(ItemType.QUANTIFIED);
      item.setQuantified(ItemMapper.toQuantifiedEntity(req.getQuantified()));
      item.setCategory(resolveCategory(item.getCategory(), req.getQuantified()));
      item.setName(resolveQuantifiedItemName(item.getQuantified()));
      return;
    }
    if (item.getItemType() != ItemType.QUANTIFIED || item.getQuantified() == null) {
      throw new BadRequestException(MSG_ITEM_NOT_QUANTIFIED);
    }
    boolean quantifiedChanged = hasQuantifiedChanged(item.getQuantified(), req);
    ItemMapper.updateQuantifiedEntity(item.getQuantified(), req.getQuantified());
    if (quantifiedChanged) {
      lockGeneratedQuantifiedItem(item);
    }
    item.setCategory(resolveCategory(item.getCategory(), req.getQuantified()));
    item.setName(resolveQuantifiedItemName(item.getQuantified()));
  }

  private static void revertToPlain(Item item) {
    item.setItemType(ItemType.PLAIN);
    item.setQuantified(null);
  }

  private void updateAssignedMember(UUID listId, Item item, UpdateItemRequest req) {
    if (!req.isAssignedMemberIdPresent()) {
      return;
    }

    UUID assignedMemberId = req.getAssignedMemberId();
    if (assignedMemberId != null && !memberRepo.existsByIdAndItemListId(assignedMemberId, listId)) {
      throw new BadRequestException(MSG_ASSIGNED_MEMBER_NOT_IN_LIST);
    }
    item.setAssignedMemberId(assignedMemberId);
  }

  private void updateCompletion(UUID listId, Item item, UpdateItemRequest req) {
    if (req.getCompleted() == null) {
      return;
    }
    if (req.getCompleted()) {
      completeItem(listId, item, req);
      return;
    }
    item.setCompleted(false);
    item.setCompletedByMemberId(null);
    item.setCompletedAt(null);
  }

  private void completeItem(UUID listId, Item item, UpdateItemRequest req) {
    if (req.getCompletedByMemberId() == null) {
      throw new BadRequestException(MSG_COMPLETED_BY_NOT_SPECIFIED);
    }
    boolean exists = memberRepo.existsByIdAndItemListId(req.getCompletedByMemberId(), listId);
    if (!exists) {
      throw new BadRequestException(MSG_MEMBER_NOT_IN_LIST);
    }
    item.setCompleted(true);
    item.setCompletedByMemberId(req.getCompletedByMemberId());
    item.setCompletedAt(Instant.now());
  }

  private static void lockQuantifiedItem(Item item) {
    if (item.getItemType() == ItemType.QUANTIFIED && item.getQuantified() != null) {
      lockGeneratedQuantifiedItem(item);
    }
  }

  private static void syncQuantifiedNameFromItemName(Item item) {
    if (item.getItemType() == ItemType.QUANTIFIED && item.getQuantified() != null) {
      item.getQuantified().setName(item.getName().trim());
    }
  }

  private static void lockGeneratedQuantifiedItem(Item item) {
    if (item.getQuantified().getOrigin() == Origin.GENERATED) {
      item.getQuantified().setRegenerationPolicy(RegenerationPolicy.LOCKED);
    }
  }

  private static boolean hasQuantifiedChanged(ItemQuantified current, UpdateItemRequest req) {
    return !Objects.equals(current.getName(), req.getQuantified().getName().trim())
        || current.getQuantity() != req.getQuantified().getQuantity()
        || current.getBaseUnit() != req.getQuantified().getBaseUnit()
        || !Objects.equals(
            current.getGeneratorKey(),
            normalizeNullableText(req.getQuantified().getGeneratorKey()));
  }

  private static ItemCategory resolveCategory(
      ItemCategory requestedCategory, com.atoook.otsukailist.dto.QuantifiedItemRequest quantified) {
    if (quantified == null
        || quantified.getOrigin() != Origin.GENERATED
        || quantified.getRegenerationPolicy() != RegenerationPolicy.AUTO) {
      return requestedCategory;
    }

    String generatorKey = normalizeNullableText(quantified.getGeneratorKey());
    if (generatorKey == null) {
      return requestedCategory;
    }

    BBQGenerationRules.BBQGenerationRule rule = BBQGenerationRules.VALUES.get(generatorKey);
    return rule == null ? requestedCategory : rule.category();
  }

  private static void validateGeneratorKey(
      com.atoook.otsukailist.dto.QuantifiedItemRequest quantified) {
    if (quantified.getOrigin() == Origin.GENERATED
        && normalizeNullableText(quantified.getGeneratorKey()) == null) {
      throw new BadRequestException(MSG_GENERATOR_KEY_REQUIRED);
    }
  }

  private static String normalizeNullableText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static String resolveItemName(CreateItemRequest req, ItemType itemType) {
    if (itemType == ItemType.QUANTIFIED && req.getQuantified() != null) {
      ItemQuantified quantified = ItemMapper.toQuantifiedEntity(req.getQuantified());
      return resolveQuantifiedItemName(quantified);
    }
    return req.getName().trim();
  }

  private static String resolveQuantifiedItemName(ItemQuantified quantified) {
    return quantified.getName().trim();
  }
}
