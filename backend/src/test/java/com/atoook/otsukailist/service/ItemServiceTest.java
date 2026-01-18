package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemRepository;
import com.atoook.otsukailist.repository.ItemListRepository;

/**
 * ItemService のユニットテスト
 * セキュリティファーストの設計思想に基づき、リストIDによるスコープ制限を重点的にテスト
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @InjectMocks
    private ItemService itemService;

    @Nested
    @DisplayName("アイテム一覧取得のテスト")
    class GetItemsTests {

        @Test
        @DisplayName("正常系：指定されたリストのアイテム一覧を取得")
        void getItemsByListId_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            List<Item> mockItems = Arrays.asList(
                    createMockItem(UUID.randomUUID(), "牛乳", false, listId),
                    createMockItem(UUID.randomUUID(), "パン", true, listId));

            when(itemRepository.findByItemListId(listId))
                    .thenReturn(mockItems);

            // When
            List<ItemResponse> result = itemService.getItemsByListId(listId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("牛乳");
            assertThat(result.get(0).isCompleted()).isFalse();
            assertThat(result.get(1).getName()).isEqualTo("パン");
            assertThat(result.get(1).isCompleted()).isTrue();

            verify(itemRepository).findByItemListId(listId);
        }

        @Test
        @DisplayName("正常系：空のリストの場合")
        void getItemsByListId_EmptyList() {
            // Given
            UUID listId = UUID.randomUUID();

            when(itemRepository.findByItemListId(listId))
                    .thenReturn(Arrays.asList());

            // When
            List<ItemResponse> result = itemService.getItemsByListId(listId);

            // Then
            assertThat(result).isEmpty();
            verify(itemRepository).findByItemListId(listId);
        }
    }

    @Nested
    @DisplayName("個別アイテム取得のテスト（セキュリティ重要）")
    class GetItemByIdTests {

        @Test
        @DisplayName("正常系：アイテム取得 - 正しいリストIDとアイテムIDの組み合わせ")
        void getItemById_ValidAccess_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            Item mockItem = createMockItem(itemId, "テストアイテム", false, listId);

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.of(mockItem));

            // When
            Optional<ItemResponse> result = itemService.getItemById(listId, itemId);

            // Then
            assertThat(result).isPresent();
            ItemResponse response = result.get();
            assertThat(response.getId()).isEqualTo(itemId);
            assertThat(response.getName()).isEqualTo("テストアイテム");
            assertThat(response.getListId()).isEqualTo(listId);

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
        }

        @Test
        @DisplayName("セキュリティテスト：異なるリストのアイテムにアクセス不可")
        void getItemById_InvalidListAccess_ReturnsEmpty() {
            // Given
            @SuppressWarnings("unused")
            UUID correctListId = UUID.randomUUID();
            UUID wrongListId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            when(itemRepository.findByIdAndItemListId(itemId, wrongListId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemResponse> result = itemService.getItemById(wrongListId, itemId);

            // Then
            assertThat(result).isEmpty();
            verify(itemRepository).findByIdAndItemListId(itemId, wrongListId);
        }
    }

    @Nested
    @DisplayName("アイテム作成のテスト")
    class CreateItemTests {

        @Test
        @DisplayName("正常系：アイテム作成 - 有効なリストIDの場合")
        void createItem_ValidList_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            CreateItemRequest request = CreateItemRequest.builder()
                    .name("牛乳")
                    .completed(false)
                    .build();

            ItemList mockItemList = createMockItemList(listId, "テストリスト");
            Item savedItem = createMockItem(UUID.randomUUID(), "新しいアイテム", false, listId);

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.of(mockItemList));
            when(itemRepository.save(any(Item.class)))
                    .thenReturn(savedItem);

            // When
            Optional<ItemResponse> result = itemService.createItem(listId, request);

            // Then
            assertThat(result).isPresent();
            ItemResponse response = result.get();
            assertThat(response.getName()).isEqualTo("新しいアイテム");
            assertThat(response.isCompleted()).isFalse();
            assertThat(response.getListId()).isEqualTo(listId);

            verify(itemListRepository).findById(listId);
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("セキュリティテスト：存在しないリストにアイテム作成不可")
        void createItem_InvalidList_ReturnsEmpty() {
            // Given
            UUID invalidListId = UUID.randomUUID();
            CreateItemRequest request = CreateItemRequest.builder()
                    .name("新しいアイテム")
                    .build();

            when(itemListRepository.findById(invalidListId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemResponse> result = itemService.createItem(invalidListId, request);

            // Then
            assertThat(result).isEmpty();

            verify(itemListRepository).findById(invalidListId);
            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Nested
    @DisplayName("アイテム更新のテスト（セキュリティ重要）")
    class UpdateItemTests {

        @Test
        @DisplayName("正常系：アイテム更新 - 正しいアクセス権限")
        void updateItem_ValidAccess_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .name("更新されたアイテム")
                    .completed(true)
                    .build();

            Item existingItem = createMockItem(itemId, "元のアイテム", false, listId);
            Item updatedItem = createMockItem(itemId, "更新されたアイテム", true, listId);

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.of(existingItem));
            when(itemRepository.save(existingItem))
                    .thenReturn(updatedItem);

            // When
            Optional<ItemResponse> result = itemService.updateItem(listId, itemId, request);

            // Then
            assertThat(result).isPresent();
            ItemResponse response = result.get();
            assertThat(response.getName()).isEqualTo("更新されたアイテム");
            assertThat(response.isCompleted()).isTrue();

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
            verify(itemRepository).save(existingItem);
        }

        @Test
        @DisplayName("セキュリティテスト：アクセス権限なしで更新不可")
        void updateItem_InvalidAccess_ReturnsEmpty() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .name("更新されたアイテム")
                    .build();

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemResponse> result = itemService.updateItem(listId, itemId, request);

            // Then
            assertThat(result).isEmpty();

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Nested
    @DisplayName("アイテム削除のテスト（セキュリティ重要）")
    class DeleteItemTests {

        @Test
        @DisplayName("正常系：アイテム削除 - 正しいアクセス権限")
        void deleteItem_ValidAccess_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            when(itemRepository.existsByIdAndItemListId(itemId, listId))
                    .thenReturn(true);

            // When
            boolean result = itemService.deleteItem(listId, itemId);

            // Then
            assertThat(result).isTrue();

            verify(itemRepository).existsByIdAndItemListId(itemId, listId);
            verify(itemRepository).deleteById(itemId);
        }

        @Test
        @DisplayName("セキュリティテスト：アクセス権限なしで削除不可")
        void deleteItem_InvalidAccess_ReturnsFalse() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            when(itemRepository.existsByIdAndItemListId(itemId, listId))
                    .thenReturn(false);

            // When
            boolean result = itemService.deleteItem(listId, itemId);

            // Then
            assertThat(result).isFalse();

            verify(itemRepository).existsByIdAndItemListId(itemId, listId);
            verify(itemRepository, never()).deleteById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("チェック状態切り替えのテスト")
    class ToggleItemCheckTests {

        @Test
        @DisplayName("正常系：チェック状態切り替え - false → true")
        void toggleItemCheck_FromFalseToTrue_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            Item existingItem = createMockItem(itemId, "テストアイテム", false, listId);
            Item toggledItem = createMockItem(itemId, "テストアイテム", true, listId);

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.of(existingItem));
            when(itemRepository.save(existingItem))
                    .thenReturn(toggledItem);

            // When
            Optional<ItemResponse> result = itemService.toggleItemCheck(listId, itemId);

            // Then
            assertThat(result).isPresent();
            ItemResponse response = result.get();
            assertThat(response.isCompleted()).isTrue();

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
            verify(itemRepository).save(existingItem);
        }

        @Test
        @DisplayName("正常系：チェック状態切り替え - true → false")
        void toggleItemCheck_FromTrueToFalse_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            Item existingItem = createMockItem(itemId, "テストアイテム", true, listId);
            Item toggledItem = createMockItem(itemId, "テストアイテム", false, listId);

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.of(existingItem));
            when(itemRepository.save(existingItem))
                    .thenReturn(toggledItem);

            // When
            Optional<ItemResponse> result = itemService.toggleItemCheck(listId, itemId);

            // Then
            assertThat(result).isPresent();
            ItemResponse response = result.get();
            assertThat(response.isCompleted()).isFalse();

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
            verify(itemRepository).save(existingItem);
        }

        @Test
        @DisplayName("セキュリティテスト：アクセス権限なしで切り替え不可")
        void toggleItemCheck_InvalidAccess_ReturnsEmpty() {
            // Given
            UUID listId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            when(itemRepository.findByIdAndItemListId(itemId, listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemResponse> result = itemService.toggleItemCheck(listId, itemId);

            // Then
            assertThat(result).isEmpty();

            verify(itemRepository).findByIdAndItemListId(itemId, listId);
            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    /**
     * テスト用のモックItemエンティティ作成
     */
    private Item createMockItem(UUID id, String name, boolean completed, UUID listId) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setCompleted(completed);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        // ItemList の設定
        ItemList itemList = new ItemList();
        itemList.setId(listId);
        item.setItemList(itemList);

        return item;
    }

    /**
     * テスト用のモックItemListエンティティ作成
     */
    private ItemList createMockItemList(UUID id, String name) {
        ItemList itemList = new ItemList();
        itemList.setId(id);
        itemList.setName(name);
        itemList.setCreatedAt(LocalDateTime.now());
        itemList.setUpdatedAt(LocalDateTime.now());
        return itemList;
    }
}
