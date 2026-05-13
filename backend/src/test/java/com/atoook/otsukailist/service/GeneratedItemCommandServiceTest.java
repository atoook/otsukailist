package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.SyncGeneratedItemsRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemCategory;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ItemQuantified;
import com.atoook.otsukailist.model.ItemType;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratedItemCommandServiceTest {

  @Mock private ItemRepository itemRepo;
  @Mock private ItemListRepository itemListRepo;
  @Mock private ListRevisionService listRevisionService;

  private GeneratedItemCommandService service;

  @BeforeEach
  void setUp() {
    GeneratedItemSyncCommandService generatedItemSyncCommandService =
        new GeneratedItemSyncCommandService(itemRepo, itemListRepo, listRevisionService);
    service = new GeneratedItemCommandService(generatedItemSyncCommandService);
  }

  @Test
  @DisplayName("既存のgenerated+auto itemはlockせず数量を更新し名称を保持すること")
  void syncGeneratedItemsUpdatesExistingAutoItemWithoutLockingOrRenaming() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList(listId);
    Item existingItem = generatedAutoItem(list, "牛肉カスタム", 1000L, "beef");
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("beef"))
            .items(List.of(generatedItemRequest("牛肉", 1500L, "beef")))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.findGeneratedItemsByGeneratorKeys(listId, List.of("beef")))
        .thenReturn(List.of(existingItem));
    when(itemRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(2L);

    var result = service.syncGeneratedItems(listId, request);

    assertThat(result.getRevision()).isEqualTo(2L);
    assertThat(existingItem.getName()).isEqualTo("牛肉カスタム");
    assertThat(existingItem.getQuantified().getQuantity()).isEqualTo(1500L);
    assertThat(existingItem.getQuantified().getRegenerationPolicy())
        .isEqualTo(RegenerationPolicy.AUTO);
    assertThat(result.getData().getItems()).hasSize(1);
    assertThat(result.getData().getItems().get(0).getName()).isEqualTo("牛肉カスタム");
    assertThat(result.getData().getDeletedItemIds()).isEmpty();
  }

  @Test
  @DisplayName("既存auto itemがない生成候補は新規itemとして追加すること")
  void syncGeneratedItemsCreatesMissingCandidate() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList(listId);
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("yakisoba"))
            .items(List.of(generatedItemRequest("焼きそば", 4L, "yakisoba")))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.findGeneratedItemsByGeneratorKeys(listId, List.of("yakisoba")))
        .thenReturn(List.of());
    when(itemRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.syncGeneratedItems(listId, request);

    assertThat(result.getData().getItems()).hasSize(1);
    assertThat(result.getData().getItems().get(0).getName()).isEqualTo("焼きそば");
    assertThat(result.getData().getItems().get(0).getCategory()).isEqualTo(ItemCategory.STAPLE);
    assertThat(result.getData().getItems().get(0).getQuantified().getGeneratorKey())
        .isEqualTo("yakisoba");
    assertThat(result.getData().getDeletedItemIds()).isEmpty();
  }

  @Test
  @DisplayName("既存のgenerated+locked itemは更新も重複追加もしないこと")
  void syncGeneratedItemsSkipsExistingLockedItem() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList(listId);
    Item existingItem = generatedLockedItem(list, "牛肉カスタム", 1000L, "beef");
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("beef"))
            .items(List.of(generatedItemRequest("牛肉", 1500L, "beef")))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.findGeneratedItemsByGeneratorKeys(listId, List.of("beef")))
        .thenReturn(List.of(existingItem));
    when(itemRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(2L);

    var result = service.syncGeneratedItems(listId, request);

    assertThat(existingItem.getQuantified().getQuantity()).isEqualTo(1000L);
    assertThat(existingItem.getQuantified().getRegenerationPolicy())
        .isEqualTo(RegenerationPolicy.LOCKED);
    assertThat(result.getData().getItems()).isEmpty();
    assertThat(result.getData().getDeletedItemIds()).isEmpty();
  }

  @Test
  @DisplayName("生成候補から外れた既存generated+auto itemは削除すること")
  void syncGeneratedItemsDeletesExistingAutoItemMissingFromCandidates() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList(listId);
    Item seafood = generatedAutoItem(list, "えび", 1L, "seafood_shrimp");
    seafood.setId(UUID.randomUUID());
    seafood.setCategory(ItemCategory.SEAFOOD);
    seafood.getQuantified().setBaseUnit(BaseUnit.PACK);
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("beef", "seafood_shrimp"))
            .items(List.of(generatedItemRequest("牛肉", 1500L, "beef")))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.findGeneratedItemsByGeneratorKeys(listId, List.of("beef", "seafood_shrimp")))
        .thenReturn(List.of(seafood));
    when(itemRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(3L);

    var result = service.syncGeneratedItems(listId, request);

    assertThat(result.getRevision()).isEqualTo(3L);
    assertThat(result.getData().getItems()).hasSize(1);
    assertThat(result.getData().getDeletedItemIds()).containsExactly(seafood.getId());
  }

  @Test
  @DisplayName("生成候補から外れた既存generated+locked itemは削除しないこと")
  void syncGeneratedItemsKeepsExistingLockedItemMissingFromCandidates() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList(listId);
    Item seafood = generatedLockedItem(list, "えび", 1L, "seafood_shrimp");
    seafood.setId(UUID.randomUUID());
    seafood.setCategory(ItemCategory.SEAFOOD);
    seafood.getQuantified().setBaseUnit(BaseUnit.PACK);
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("beef", "seafood_shrimp"))
            .items(List.of(generatedItemRequest("牛肉", 1500L, "beef")))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.findGeneratedItemsByGeneratorKeys(listId, List.of("beef", "seafood_shrimp")))
        .thenReturn(List.of(seafood));
    when(itemRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(4L);

    var result = service.syncGeneratedItems(listId, request);

    assertThat(result.getData().getItems()).hasSize(1);
    assertThat(result.getData().getDeletedItemIds()).isEmpty();
  }

  @Test
  @DisplayName("生成候補のgeneratorKeyがscope外の場合はエラーにすること")
  void syncGeneratedItemsRejectsCandidateOutsideScope() {
    UUID listId = UUID.randomUUID();
    SyncGeneratedItemsRequest request =
        SyncGeneratedItemsRequest.builder()
            .generatorKeysInScope(List.of("beef"))
            .items(List.of(generatedItemRequest("焼きそば", 4L, "yakisoba")))
            .build();

    assertThatThrownBy(() -> service.syncGeneratedItems(listId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("生成ルールID一覧が不正です");
  }

  private static ItemList itemList(UUID listId) {
    ItemList list = new ItemList();
    list.setId(listId);
    list.setName("BBQ");
    return list;
  }

  private static Item generatedAutoItem(
      ItemList list, String name, long quantity, String generatorKey) {
    Item item = new Item();
    item.setName(name);
    item.setItemType(ItemType.QUANTIFIED);
    item.setCategory(ItemCategory.MEAT);
    item.setItemList(list);
    item.setQuantified(quantified(quantity, generatorKey));
    return item;
  }

  private static Item generatedLockedItem(
      ItemList list, String name, long quantity, String generatorKey) {
    Item item = generatedAutoItem(list, name, quantity, generatorKey);
    item.getQuantified().setRegenerationPolicy(RegenerationPolicy.LOCKED);
    return item;
  }

  private static CreateItemRequest generatedItemRequest(
      String name, long quantity, String generatorKey) {
    return CreateItemRequest.builder()
        .name(name)
        .itemType(ItemType.QUANTIFIED)
        .quantified(
            QuantifiedItemRequest.builder()
                .quantity(quantity)
                .baseUnit(resolveBaseUnit(generatorKey))
                .origin(Origin.GENERATED)
                .regenerationPolicy(RegenerationPolicy.AUTO)
                .generatorKey(generatorKey)
                .build())
        .build();
  }

  private static ItemQuantified quantified(long quantity, String generatorKey) {
    ItemQuantified quantified = new ItemQuantified();
    quantified.setQuantity(quantity);
    quantified.setBaseUnit(BaseUnit.G);
    quantified.setOrigin(Origin.GENERATED);
    quantified.setRegenerationPolicy(RegenerationPolicy.AUTO);
    quantified.setGeneratorKey(generatorKey);
    return quantified;
  }

  private static BaseUnit resolveBaseUnit(String generatorKey) {
    return switch (generatorKey) {
      case "yakisoba" -> BaseUnit.PIECE;
      case "seafood_shrimp" -> BaseUnit.PACK;
      default -> BaseUnit.G;
    };
  }
}
