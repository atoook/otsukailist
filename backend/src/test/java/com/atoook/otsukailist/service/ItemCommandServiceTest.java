package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.model.BaseUnit;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemCommandServiceTest {

  @Mock private ItemRepository itemRepo;
  @Mock private ItemListRepository itemListRepo;
  @Mock private MemberRepository memberRepo;
  @Mock private ListRevisionService listRevisionService;

  private ItemCommandService service;

  @BeforeEach
  void setUp() {
    service = new ItemCommandService(itemRepo, itemListRepo, memberRepo, listRevisionService);
  }

  @Test
  @DisplayName("作成時はitemType未指定なら通常アイテムとして保存すること")
  void createItemDefaultsToPlainItem() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList("買い物");
    CreateItemRequest request = CreateItemRequest.builder().name(" 牛乳 ").build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.createItem(listId, request);

    assertThat(result.getData().getName()).isEqualTo("牛乳");
    assertThat(result.getData().getItemType()).isEqualTo(ItemType.PLAIN);
    assertThat(result.getData().getQuantified()).isNull();
  }

  @Test
  @DisplayName("作成時に指定された準備方法を保存すること")
  void createItemStoresPreparationType() {
    UUID listId = UUID.randomUUID();
    ItemList list = itemList("買い物");
    CreateItemRequest request =
        CreateItemRequest.builder().name("包丁").preparationType(ItemPreparationType.BRING).build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(itemRepo.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.createItem(listId, request);

    assertThat(result.getData().getPreparationType()).isEqualTo(ItemPreparationType.BRING);
  }

  @Test
  @DisplayName("数量付きアイテム作成時は詳細情報が必須であること")
  void createQuantifiedItemRequiresQuantifiedDetails() {
    UUID listId = UUID.randomUUID();
    CreateItemRequest request =
        CreateItemRequest.builder().name("牛肉").itemType(ItemType.QUANTIFIED).build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(itemList("買い物")));

    assertThatThrownBy(() -> service.createItem(listId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("数量付きアイテム情報が未指定です");
  }

  @Test
  @DisplayName("通常アイテム作成時に数量付き詳細を送った場合は拒否すること")
  void createPlainItemRejectsQuantifiedDetails() {
    UUID listId = UUID.randomUUID();
    CreateItemRequest request =
        CreateItemRequest.builder()
            .name("牛肉")
            .itemType(ItemType.PLAIN)
            .quantified(manualNoneQuantifiedRequest(1000L))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(itemList("買い物")));

    assertThatThrownBy(() -> service.createItem(listId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("通常アイテムに数量付きアイテム情報は指定できません");
  }

  @Test
  @DisplayName("自動生成アイテム作成時は既知の生成ルールからカテゴリを解決すること")
  void createGeneratedItemResolvesCategoryFromKnownGeneratorKey() {
    UUID listId = UUID.randomUUID();
    CreateItemRequest request =
        CreateItemRequest.builder()
            .name("牛肉")
            .itemType(ItemType.QUANTIFIED)
            .quantified(generatedAutoQuantifiedRequest(1000L, "beef"))
            .build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(itemList("買い物")));
    when(itemRepo.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.createItem(listId, request);

    assertThat(result.getData().getItemType()).isEqualTo(ItemType.QUANTIFIED);
    assertThat(result.getData().getCategory()).isEqualTo(ItemCategory.MEAT);
    assertThat(result.getData().getQuantified().getGeneratorKey()).isEqualTo("beef");
  }

  @Test
  @DisplayName("更新時に空白のみの名前を送った場合は拒否すること")
  void updateItemRejectsBlankNameAfterTrim() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UpdateItemRequest request = UpdateItemRequest.builder().name("   ").build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(plainItem("牛乳")));

    assertThatThrownBy(() -> service.updateItem(listId, itemId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("アイテム名は必須です");
  }

  @Test
  @DisplayName("数量付きアイテムの不正なorigin/policy組み合わせは拒否すること")
  void updateQuantifiedItemRejectsInvalidOriginPolicyCombination() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .quantified(
                QuantifiedItemRequest.builder()
                    .quantity(1000L)
                    .baseUnit(BaseUnit.G)
                    .origin(Origin.MANUAL)
                    .regenerationPolicy(RegenerationPolicy.AUTO)
                    .build())
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(plainItem("牛肉")));

    assertThatThrownBy(() -> service.updateItem(listId, itemId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("数量付きアイテムの生成状態が不正です");
  }

  @Test
  @DisplayName("自動生成アイテムの未知の生成ルールIDは拒否すること")
  void updateGeneratedItemRejectsUnknownGeneratorKey() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .quantified(generatedAutoQuantifiedRequest(1000L, "unknown"))
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(plainItem("牛肉")));

    assertThatThrownBy(() -> service.updateItem(listId, itemId, request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("未知の生成ルールIDです");
  }

  @Test
  @DisplayName("数量付きアイテム更新時は item name を名称として保持し数量単位を混ぜないこと")
  void updateQuantifiedItemKeepsItemNameAsDisplayName() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉 1000g", 1000L);
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .name("直接編集された名前")
            .itemType(ItemType.QUANTIFIED)
            .quantified(
                QuantifiedItemRequest.builder()
                    .quantity(1200L)
                    .baseUnit(BaseUnit.G)
                    .origin(Origin.GENERATED)
                    .regenerationPolicy(RegenerationPolicy.AUTO)
                    .generatorKey("beef")
                    .build())
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.updateItem(listId, itemId, request);

    assertThat(item.getName()).isEqualTo("直接編集された名前");
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.LOCKED);
    assertThat(result.getData().getName()).isEqualTo("直接編集された名前");
  }

  @Test
  @DisplayName("数量付きアイテムの表示名だけを更新した場合は item name のみ変更しロックしないこと")
  void updateQuantifiedItemDirectNameEditChangesOnlyItemNameWithoutLocking() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉 1000g", 1000L);
    UpdateItemRequest request = UpdateItemRequest.builder().name("直接編集された名前").build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.updateItem(listId, itemId, request);

    assertThat(item.getName()).isEqualTo("直接編集された名前");
    assertThat(item.getQuantified().getQuantity()).isEqualTo(1000L);
    assertThat(item.getQuantified().getBaseUnit()).isEqualTo(BaseUnit.G);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.AUTO);
    assertThat(result.getData().getName()).isEqualTo("直接編集された名前");
  }

  @Test
  @DisplayName("更新時に準備方法を変更できること")
  void updateItemChangesPreparationType() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = plainItem("包丁");
    item.setPreparationType(ItemPreparationType.BUY);
    UpdateItemRequest request =
        UpdateItemRequest.builder().preparationType(ItemPreparationType.BRING).build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.updateItem(listId, itemId, request);

    assertThat(item.getPreparationType()).isEqualTo(ItemPreparationType.BRING);
    assertThat(result.getData().getPreparationType()).isEqualTo(ItemPreparationType.BRING);
  }

  @Test
  @DisplayName("itemType未指定でも数量付き詳細があれば通常アイテムから数量付きアイテムへ変換すること")
  void updatePlainItemWithQuantifiedDetailsConvertsToQuantifiedWithoutItemType() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = plainItem("牛肉");
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .quantified(
                QuantifiedItemRequest.builder()
                    .quantity(1000L)
                    .baseUnit(BaseUnit.G)
                    .origin(Origin.MANUAL)
                    .regenerationPolicy(RegenerationPolicy.NONE)
                    .build())
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.updateItem(listId, itemId, request);

    assertThat(item.getItemType()).isEqualTo(ItemType.QUANTIFIED);
    assertThat(item.getName()).isEqualTo("牛肉");
    assertThat(item.getQuantified()).isNotNull();
    assertThat(item.getQuantified().getQuantity()).isEqualTo(1000L);
    assertThat(item.getQuantified().getOrigin()).isEqualTo(Origin.MANUAL);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.NONE);
    assertThat(result.getData().getItemType()).isEqualTo(ItemType.QUANTIFIED);
  }

  @Test
  @DisplayName("generated auto の数量付き詳細が変わらない場合は生成ルールのカテゴリを反映してロックしないこと")
  void updateGeneratedAutoWithoutDetailChangeResolvesGeneratedCategoryWithoutLocking() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉", 1000L);
    item.setCategory(null);
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .itemType(ItemType.QUANTIFIED)
            .quantified(generatedAutoQuantifiedRequest(1000L, "beef"))
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    service.updateItem(listId, itemId, request);

    assertThat(item.getCategory()).isEqualTo(ItemCategory.MEAT);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.AUTO);
  }

  @Test
  @DisplayName("generated auto のカテゴリを明示変更した場合は指定カテゴリを保持してロックすること")
  void updateGeneratedAutoCategoryKeepsRequestedCategoryAndLocks() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉", 1000L);
    item.setCategory(ItemCategory.MEAT);
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .category(ItemCategory.SWEETS)
            .itemType(ItemType.QUANTIFIED)
            .quantified(generatedAutoQuantifiedRequest(1000L, "beef"))
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    service.updateItem(listId, itemId, request);

    assertThat(item.getCategory()).isEqualTo(ItemCategory.SWEETS);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.LOCKED);
  }

  @Test
  @DisplayName("generated auto のルール由来カテゴリが送られた場合はユーザー編集扱いにしないこと")
  void updateGeneratedAutoRuleCategoryDoesNotLock() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉", 1000L);
    item.setCategory(null);
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .category(ItemCategory.MEAT)
            .itemType(ItemType.QUANTIFIED)
            .quantified(generatedAutoQuantifiedRequest(1000L, "beef"))
            .build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    service.updateItem(listId, itemId, request);

    assertThat(item.getCategory()).isEqualTo(ItemCategory.MEAT);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.AUTO);
  }

  private static Item plainItem(String itemName) {
    Item item = new Item();
    item.setName(itemName);
    item.setItemType(ItemType.PLAIN);

    return item;
  }

  private static ItemList itemList(String name) {
    ItemList itemList = new ItemList();
    itemList.setName(name);

    return itemList;
  }

  private static QuantifiedItemRequest manualNoneQuantifiedRequest(long quantity) {
    return QuantifiedItemRequest.builder()
        .quantity(quantity)
        .baseUnit(BaseUnit.G)
        .origin(Origin.MANUAL)
        .regenerationPolicy(RegenerationPolicy.NONE)
        .build();
  }

  private static Item quantifiedItem(String itemName, long quantity) {
    Item item = new Item();
    item.setName(itemName);
    item.setItemType(ItemType.QUANTIFIED);

    ItemQuantified quantified = new ItemQuantified();
    quantified.setQuantity(quantity);
    quantified.setBaseUnit(BaseUnit.G);
    quantified.setOrigin(Origin.GENERATED);
    quantified.setRegenerationPolicy(RegenerationPolicy.AUTO);
    quantified.setGeneratorKey("beef");
    item.setQuantified(quantified);

    return item;
  }

  private static QuantifiedItemRequest generatedAutoQuantifiedRequest(
      long quantity, String generatorKey) {
    return QuantifiedItemRequest.builder()
        .quantity(quantity)
        .baseUnit(BaseUnit.G)
        .origin(Origin.GENERATED)
        .regenerationPolicy(RegenerationPolicy.AUTO)
        .generatorKey(generatorKey)
        .build();
  }
}
