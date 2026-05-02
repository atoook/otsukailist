package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.Item;
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
  @DisplayName("数量付きアイテム更新時は詳細名を item name として保存し数量単位を混ぜないこと")
  void updateQuantifiedItemStoresOnlyQuantifiedNameAsItemName() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉 1000g", "牛肉", 1000L);
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .name("直接編集された名前")
            .itemType(ItemType.QUANTIFIED)
            .quantified(
                QuantifiedItemRequest.builder()
                    .name("牛肉")
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

    assertThat(item.getName()).isEqualTo("牛肉");
    assertThat(item.getQuantified().getName()).isEqualTo("牛肉");
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.LOCKED);
    assertThat(result.getData().getName()).isEqualTo("牛肉");
  }

  @Test
  @DisplayName("数量付きアイテムの表示名だけを更新した場合は詳細名にも同期すること")
  void updateQuantifiedItemDirectNameEditSyncsQuantifiedName() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = quantifiedItem("牛肉 1000g", "牛肉", 1000L);
    UpdateItemRequest request = UpdateItemRequest.builder().name("直接編集された名前").build();

    when(itemRepo.findByIdAndItemListId(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(1L);

    var result = service.updateItem(listId, itemId, request);

    assertThat(item.getName()).isEqualTo("直接編集された名前");
    assertThat(item.getQuantified().getName()).isEqualTo("直接編集された名前");
    assertThat(item.getQuantified().getQuantity()).isEqualTo(1000L);
    assertThat(item.getQuantified().getBaseUnit()).isEqualTo(BaseUnit.G);
    assertThat(item.getQuantified().getRegenerationPolicy()).isEqualTo(RegenerationPolicy.LOCKED);
    assertThat(result.getData().getName()).isEqualTo("直接編集された名前");
  }

  private static Item quantifiedItem(String itemName, String quantifiedName, long quantity) {
    Item item = new Item();
    item.setName(itemName);
    item.setItemType(ItemType.QUANTIFIED);

    ItemQuantified quantified = new ItemQuantified();
    quantified.setName(quantifiedName);
    quantified.setQuantity(quantity);
    quantified.setBaseUnit(BaseUnit.G);
    quantified.setOrigin(Origin.GENERATED);
    quantified.setRegenerationPolicy(RegenerationPolicy.AUTO);
    quantified.setGeneratorKey("beef");
    item.setQuantified(quantified);

    return item;
  }
}
