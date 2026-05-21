package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.dto.MarkItemCompletedRequest;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemType;
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
class ItemCompletionServiceTest {

  @Mock private ItemRepository itemRepo;
  @Mock private ItemListRepository itemListRepo;
  @Mock private MemberRepository memberRepo;
  @Mock private ListRevisionService listRevisionService;

  private ItemCompletionService service;

  @BeforeEach
  void setUp() {
    service = new ItemCompletionService(itemRepo, itemListRepo, memberRepo, listRevisionService);
  }

  @Test
  @DisplayName("markCompletedは未完了アイテムを完了にしrevisionを進めること")
  void markCompletedCompletesIncompleteItemAndIncrementsRevision() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Item item = plainItem("牛乳");
    MarkItemCompletedRequest request =
        MarkItemCompletedRequest.builder().completedByMemberId(memberId).build();

    when(itemRepo.findByIdAndItemListIdForUpdate(itemId, listId)).thenReturn(Optional.of(item));
    when(memberRepo.existsByIdAndItemListId(memberId, listId)).thenReturn(true);
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(2L);

    var result = service.markCompleted(listId, itemId, request);

    assertThat(item.isCompleted()).isTrue();
    assertThat(item.getCompletedByMemberId()).isEqualTo(memberId);
    assertThat(item.getCompletedAt()).isNotNull();
    assertThat(result.getRevision()).isEqualTo(2L);
    assertThat(result.getChanged()).isTrue();
    assertThat(result.getData().isCompleted()).isTrue();
  }

  @Test
  @DisplayName("markCompletedは既に完了済みなら完了者を上書きせずno-opにすること")
  void markCompletedNoopsWhenAlreadyCompleted() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UUID originalMemberId = UUID.randomUUID();
    UUID requestedMemberId = UUID.randomUUID();
    Instant completedAt = Instant.parse("2024-01-01T00:00:00Z");
    Item item = completedItem("牛乳", originalMemberId, completedAt);
    MarkItemCompletedRequest request =
        MarkItemCompletedRequest.builder().completedByMemberId(requestedMemberId).build();

    when(itemRepo.findByIdAndItemListIdForUpdate(itemId, listId)).thenReturn(Optional.of(item));
    when(itemListRepo.findRevision(listId)).thenReturn(Optional.of(7L));

    var result = service.markCompleted(listId, itemId, request);

    assertThat(result.getRevision()).isEqualTo(7L);
    assertThat(result.getChanged()).isFalse();
    assertThat(result.getData().isCompleted()).isTrue();
    assertThat(result.getData().getCompletedByMemberId()).isEqualTo(originalMemberId);
    assertThat(result.getData().getCompletedAt()).isEqualTo(completedAt);
    verify(itemRepo, never()).save(any(Item.class));
    verify(listRevisionService, never()).incrementAndGet(listId);
  }

  @Test
  @DisplayName("markIncompleteは完了済みアイテムを未完了にしrevisionを進めること")
  void markIncompleteClearsCompletedItemAndIncrementsRevision() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    Item item = completedItem("牛乳", memberId, Instant.parse("2024-01-01T00:00:00Z"));

    when(itemRepo.findByIdAndItemListIdForUpdate(itemId, listId)).thenReturn(Optional.of(item));
    when(itemRepo.save(item)).thenReturn(item);
    when(listRevisionService.incrementAndGet(listId)).thenReturn(8L);

    var result = service.markIncomplete(listId, itemId);

    assertThat(item.isCompleted()).isFalse();
    assertThat(item.getCompletedByMemberId()).isNull();
    assertThat(item.getCompletedAt()).isNull();
    assertThat(result.getRevision()).isEqualTo(8L);
    assertThat(result.getChanged()).isTrue();
    assertThat(result.getData().isCompleted()).isFalse();
  }

  @Test
  @DisplayName("markIncompleteは他ユーザーが先に未完了へ戻していた場合も逆反転せずno-opにすること")
  void markIncompleteNoopsWhenAlreadyIncompleteAfterAnotherUserUpdate() {
    UUID listId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    Item item = plainItem("牛乳");

    when(itemRepo.findByIdAndItemListIdForUpdate(itemId, listId)).thenReturn(Optional.of(item));
    when(itemListRepo.findRevision(listId)).thenReturn(Optional.of(9L));

    var result = service.markIncomplete(listId, itemId);

    assertThat(result.getRevision()).isEqualTo(9L);
    assertThat(result.getChanged()).isFalse();
    assertThat(result.getData().isCompleted()).isFalse();
    assertThat(result.getData().getCompletedByMemberId()).isNull();
    verify(itemRepo, never()).save(any(Item.class));
    verify(listRevisionService, never()).incrementAndGet(listId);
  }

  private static Item plainItem(String itemName) {
    Item item = new Item();
    item.setName(itemName);
    item.setItemType(ItemType.PLAIN);

    return item;
  }

  private static Item completedItem(
      String itemName, UUID completedByMemberId, Instant completedAt) {
    Item item = plainItem(itemName);
    item.setCompleted(true);
    item.setCompletedByMemberId(completedByMemberId);
    item.setCompletedAt(completedAt);

    return item;
  }
}
