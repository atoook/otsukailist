package com.atoook.otsukailist.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.SyncGeneratedItemsResponse;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.mapper.ItemMapper;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemCategory;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ItemPreparationType;
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
public class GeneratedItemSyncCommandService {

  private final ItemRepository itemRepo;
  private final ItemListRepository itemListRepo;
  private final MemberRepository memberRepo;
  private final ListRevisionService listRevisionService;

  private static final String MSG_ASSIGNED_MEMBER_NOT_IN_LIST = "指定された担当者はリストのメンバーではありません";
  private static final String MSG_GENERATED_ITEM_DUPLICATED = "生成アイテムが重複しています";

  /** Generated item update command normalized from a sync request. */
  public record GeneratedItemUpdateCommand(
      String name,
      ItemCategory category,
      ItemPreparationType preparationType,
      UUID assignedMemberId,
      QuantifiedItemRequest quantified) {

    /** Returns the stable generator key for matching generated items. */
    public String generatorKey() {
      return quantified.getGeneratorKey();
    }
  }

  /**
   * Sync generated item candidates within the provided generator-key scope.
   *
   * @param listId target list ID
   * @param commands generated item update commands
   * @param generatorKeysInScope generator keys managed by the current template
   * @return synced items and deleted item IDs with latest list revision
   */
  @Transactional
  public MutationResponse<SyncGeneratedItemsResponse> syncGeneratedItems(
      UUID listId, List<GeneratedItemUpdateCommand> commands, List<String> generatorKeysInScope) {
    ItemList list =
        itemListRepo
            .findById(listId)
            .orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));

    List<GeneratedItemUpdateCommand> safeCommands = commands == null ? List.of() : commands;
    List<String> safeGeneratorKeysInScope =
        generatorKeysInScope == null ? List.of() : generatorKeysInScope;
    validateAssignedMembers(listId, safeCommands);
    Set<String> generatedKeys =
        safeCommands.stream()
            .map(GeneratedItemUpdateCommand::generatorKey)
            .collect(Collectors.toSet());
    Map<String, Item> existingItems =
        safeGeneratorKeysInScope.isEmpty()
            ? Map.of()
            : toExistingItemMap(
                itemRepo.findGeneratedItemsByGeneratorKeys(listId, safeGeneratorKeysInScope));

    List<Item> changedItems =
        safeCommands.stream()
            .map(command -> upsertGeneratedItem(list, command, existingItems))
            .filter(Objects::nonNull)
            .toList();

    List<Item> deletedItems =
        existingItems.values().stream()
            .filter(item -> !generatedKeys.contains(item.getQuantified().getGeneratorKey()))
            .filter(item -> item.getQuantified().getRegenerationPolicy() == RegenerationPolicy.AUTO)
            .toList();

    List<Item> savedItems = itemRepo.saveAll(changedItems);
    itemRepo.deleteAll(deletedItems);
    long revision = listRevisionService.incrementAndGet(listId);

    SyncGeneratedItemsResponse data =
        SyncGeneratedItemsResponse.builder()
            .items(savedItems.stream().map(ItemMapper::toResponse).toList())
            .deletedItemIds(deletedItems.stream().map(Item::getId).toList())
            .build();

    return MutationResponse.<SyncGeneratedItemsResponse>builder()
        .revision(revision)
        .data(data)
        .build();
  }

  private Item upsertGeneratedItem(
      ItemList list, GeneratedItemUpdateCommand command, Map<String, Item> existingItems) {
    Item existingItem = existingItems.get(command.generatorKey());
    if (existingItem != null) {
      if (existingItem.getQuantified().getRegenerationPolicy() == RegenerationPolicy.LOCKED) {
        return null;
      }
      updateExistingGeneratedAutoItem(existingItem, command);
      return existingItem;
    }

    return createGeneratedItem(list, command);
  }

  private void validateAssignedMembers(UUID listId, List<GeneratedItemUpdateCommand> commands) {
    for (GeneratedItemUpdateCommand command : commands) {
      UUID assignedMemberId = command.assignedMemberId();
      if (assignedMemberId != null
          && !memberRepo.existsByIdAndItemListId(assignedMemberId, listId)) {
        throw new BadRequestException(MSG_ASSIGNED_MEMBER_NOT_IN_LIST);
      }
    }
  }

  private static Map<String, Item> toExistingItemMap(List<Item> items) {
    return items.stream()
        .collect(
            Collectors.toMap(
                item -> item.getQuantified().getGeneratorKey(),
                Function.identity(),
                (left, right) -> {
                  throw new BadRequestException(MSG_GENERATED_ITEM_DUPLICATED);
                }));
  }

  private static Item createGeneratedItem(ItemList list, GeneratedItemUpdateCommand command) {
    Item item = new Item();
    item.setName(command.name());
    item.setItemType(ItemType.QUANTIFIED);
    item.setCategory(command.category());
    item.setPreparationType(command.preparationType());
    item.setCompleted(false);
    item.setAssignedMemberId(command.assignedMemberId());
    item.setCompletedByMemberId(null);
    item.setCompletedAt(null);
    item.setItemList(list);
    item.setQuantified(ItemMapper.toQuantifiedEntity(command.quantified()));
    return item;
  }

  private static void updateExistingGeneratedAutoItem(
      Item item, GeneratedItemUpdateCommand command) {
    ItemQuantified quantified = item.getQuantified();
    if (quantified == null) {
      throw new BadRequestException("生成アイテムの形式が不正です");
    }
    quantified.setQuantity(command.quantified().getQuantity());
    quantified.setBaseUnit(command.quantified().getBaseUnit());
    quantified.setOrigin(Origin.GENERATED);
    quantified.setRegenerationPolicy(RegenerationPolicy.AUTO);
    quantified.setGeneratorKey(command.generatorKey());
    item.setCategory(command.category());
  }
}
