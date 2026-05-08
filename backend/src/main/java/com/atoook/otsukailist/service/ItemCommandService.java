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
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.generation.GenerationRule;
import com.atoook.otsukailist.generation.GenerationRules;
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
  private static final String MSG_GENERATOR_KEY_UNKNOWN = "未知の生成ルールIDです";
  private static final String MSG_ITEM_NAME_REQUIRED = "アイテム名は必須です";
  private static final String MSG_QUANTIFIED_STATE_INVALID = "数量付きアイテムの生成状態が不正です";

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

    item.setName(resolveItemName(req));
    item.setItemType(itemType);
    item.setCategory(resolveCategory(req.getCategory(), req.getQuantified()));
    item.setPreparationType(req.getPreparationType());
    item.setCompleted(false);
    item.setAssignedMemberId(resolveAssignedMemberId(listId, req.getAssignedMemberId()));
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

  private UUID resolveAssignedMemberId(UUID listId, UUID assignedMemberId) {
    if (assignedMemberId != null && !memberRepo.existsByIdAndItemListId(assignedMemberId, listId)) {
      throw new BadRequestException(MSG_ASSIGNED_MEMBER_NOT_IN_LIST);
    }
    return assignedMemberId;
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

    updateNameCategoryAndQuantified(item, req);
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
      validateQuantifiedState(req.getQuantified());
    }
  }

  private static void updateNameCategoryAndQuantified(Item item, UpdateItemRequest req) {
    ItemCategory requestedCategory = resolveRequestedCategory(item, req);
    boolean categoryEdited = isUserCategoryEdit(item, req, requestedCategory);
    boolean revertingToPlain =
        item.getItemType() == ItemType.QUANTIFIED && req.getItemType() == ItemType.PLAIN;
    boolean quantifiedDetailsEdited = req.getQuantified() != null;

    updateItemName(item, req);
    item.setPreparationType(req.getPreparationTypeOrDefault(item.getPreparationType()));
    rejectQuantifiedDetailsForPlainRequest(req, quantifiedDetailsEdited);
    if (revertingToPlain) {
      item.setCategory(requestedCategory);
      revertToPlain(item);
      return;
    }
    boolean quantifiedDetailsChanged = updateQuantifiedDetails(item, req);
    boolean shouldLockGeneratedAuto = categoryEdited || quantifiedDetailsChanged;
    item.setCategory(resolveCategoryAfterUpdate(requestedCategory, req, shouldLockGeneratedAuto));
    if (shouldLockGeneratedAuto) {
      lockGeneratedAutoQuantifiedItem(item);
    }
  }

  private static void updateItemName(Item item, UpdateItemRequest req) {
    if (req.getName() != null) {
      item.setName(normalizeRequiredText(req.getName(), MSG_ITEM_NAME_REQUIRED));
    }
  }

  private static void rejectQuantifiedDetailsForPlainRequest(
      UpdateItemRequest req, boolean quantifiedDetailsEdited) {
    if (req.getItemType() == ItemType.PLAIN && quantifiedDetailsEdited) {
      throw new BadRequestException(MSG_QUANTIFIED_NOT_ALLOWED);
    }
  }

  private static boolean updateQuantifiedDetails(Item item, UpdateItemRequest req) {
    if (req.getQuantified() == null) {
      return false;
    }
    validateQuantifiedState(req.getQuantified());
    if (item.getItemType() == ItemType.PLAIN) {
      item.setItemType(ItemType.QUANTIFIED);
      item.setQuantified(ItemMapper.toQuantifiedEntity(req.getQuantified()));
      return false;
    }
    if (item.getItemType() != ItemType.QUANTIFIED || item.getQuantified() == null) {
      throw new BadRequestException(MSG_ITEM_NOT_QUANTIFIED);
    }
    boolean quantifiedChanged = hasQuantifiedChanged(item.getQuantified(), req);
    ItemMapper.updateQuantifiedEntity(item.getQuantified(), req.getQuantified());
    return quantifiedChanged;
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

  private static void lockGeneratedAutoQuantifiedItem(Item item) {
    if (item.getItemType() != ItemType.QUANTIFIED || item.getQuantified() == null) {
      return;
    }
    if (item.getQuantified().getOrigin() == Origin.GENERATED
        && item.getQuantified().getRegenerationPolicy() == RegenerationPolicy.AUTO) {
      item.getQuantified().setRegenerationPolicy(RegenerationPolicy.LOCKED);
    }
  }

  private static boolean hasQuantifiedChanged(ItemQuantified current, UpdateItemRequest req) {
    return current.getQuantity() != req.getQuantified().getQuantity()
        || current.getBaseUnit() != req.getQuantified().getBaseUnit()
        || !Objects.equals(
            current.getGeneratorKey(),
            normalizeNullableText(req.getQuantified().getGeneratorKey()));
  }

  private static ItemCategory resolveCategory(
      ItemCategory requestedCategory, QuantifiedItemRequest quantified) {
    ItemCategory generatedCategory = resolveGeneratedCategory(quantified);
    return generatedCategory == null ? requestedCategory : generatedCategory;
  }

  private static boolean isUserCategoryEdit(
      Item item, UpdateItemRequest req, ItemCategory requestedCategory) {
    if (!req.isCategoryPresent() || Objects.equals(item.getCategory(), requestedCategory)) {
      return false;
    }
    ItemCategory generatedCategory = resolveGeneratedCategory(item, req);
    return generatedCategory == null || !Objects.equals(requestedCategory, generatedCategory);
  }

  private static ItemCategory resolveGeneratedCategory(Item item, UpdateItemRequest req) {
    if (req.getQuantified() != null) {
      return resolveGeneratedCategory(req.getQuantified());
    }
    return resolveGeneratedCategory(item.getQuantified());
  }

  private static ItemCategory resolveGeneratedCategory(ItemQuantified quantified) {
    if (quantified == null
        || quantified.getOrigin() != Origin.GENERATED
        || quantified.getRegenerationPolicy() != RegenerationPolicy.AUTO) {
      return null;
    }
    return resolveGeneratedCategory(quantified.getGeneratorKey());
  }

  private static ItemCategory resolveGeneratedCategory(QuantifiedItemRequest quantified) {
    if (quantified == null
        || quantified.getOrigin() != Origin.GENERATED
        || quantified.getRegenerationPolicy() != RegenerationPolicy.AUTO) {
      return null;
    }
    return resolveGeneratedCategory(quantified.getGeneratorKey());
  }

  private static ItemCategory resolveGeneratedCategory(String generatorKey) {
    String normalizedGeneratorKey = normalizeNullableText(generatorKey);
    if (normalizedGeneratorKey == null) {
      return null;
    }
    GenerationRule rule = GenerationRules.findByGeneratorKey(normalizedGeneratorKey);
    return rule == null ? null : rule.category();
  }

  private static ItemCategory resolveRequestedCategory(Item item, UpdateItemRequest req) {
    if (req.isCategoryPresent()) {
      return req.getCategory();
    }
    return item.getCategory();
  }

  private static ItemCategory resolveCategoryAfterUpdate(
      ItemCategory requestedCategory, UpdateItemRequest req, boolean shouldLockGeneratedAuto) {
    if (shouldLockGeneratedAuto) {
      return requestedCategory;
    }
    return resolveCategory(requestedCategory, req.getQuantified());
  }

  private static void validateQuantifiedState(QuantifiedItemRequest quantified) {
    if (quantified.getOrigin() == Origin.MANUAL
        && quantified.getRegenerationPolicy() == RegenerationPolicy.NONE) {
      return;
    }
    if (quantified.getOrigin() == Origin.GENERATED
        && (quantified.getRegenerationPolicy() == RegenerationPolicy.AUTO
            || quantified.getRegenerationPolicy() == RegenerationPolicy.LOCKED)) {
      validateGeneratedRule(quantified);
      return;
    }
    throw new BadRequestException(MSG_QUANTIFIED_STATE_INVALID);
  }

  private static void validateGeneratedRule(QuantifiedItemRequest quantified) {
    String generatorKey = normalizeNullableText(quantified.getGeneratorKey());
    if (generatorKey == null) {
      throw new BadRequestException(MSG_GENERATOR_KEY_REQUIRED);
    }
    if (!GenerationRules.containsGeneratorKey(generatorKey)) {
      throw new BadRequestException(MSG_GENERATOR_KEY_UNKNOWN);
    }
  }

  private static String normalizeNullableText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static String resolveItemName(CreateItemRequest req) {
    return normalizeRequiredText(req.getName(), MSG_ITEM_NAME_REQUIRED);
  }

  private static String normalizeRequiredText(String value, String message) {
    String normalizedValue = normalizeNullableText(value);
    if (normalizedValue == null) {
      throw new BadRequestException(message);
    }
    return normalizedValue;
  }
}
