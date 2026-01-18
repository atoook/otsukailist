package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atoook.otsukailist.dto.CreateItemListRequest;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.repository.ItemListRepository;

/**
 * ItemListService のユニットテスト
 */
@ExtendWith(MockitoExtension.class)
class ItemListServiceTest {

    @Mock
    private ItemListRepository itemListRepository;

    @InjectMocks
    private ItemListService itemListService;

    @Nested
    @DisplayName("アイテムリスト取得のテスト")
    class GetItemListTests {

        @Test
        @DisplayName("正常系：IDで取得 - リストが存在する場合")
        void getItemListById_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            ItemList mockEntity = createMockItemList(listId, "テストリスト");

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.of(mockEntity));

            // When
            Optional<ItemListResponse> result = itemListService.getItemListById(listId);

            // Then
            assertThat(result).isPresent();
            ItemListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("テストリスト");

            verify(itemListRepository).findById(listId);
        }

        @Test
        @DisplayName("異常系：IDで取得 - リストが存在しない場合")
        void getItemListById_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemListResponse> result = itemListService.getItemListById(listId);

            // Then
            assertThat(result).isEmpty();
            verify(itemListRepository).findById(listId);
        }

        @Test
        @DisplayName("正常系：アイテム詳細含む取得 - リストが存在する場合")
        void getItemListByIdWithItems_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            ItemList mockEntity = createMockItemList(listId, "テストリスト");

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.of(mockEntity));

            // When
            Optional<ItemListResponse> result = itemListService.getItemListByIdWithItems(listId);

            // Then
            assertThat(result).isPresent();
            ItemListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("テストリスト");

            verify(itemListRepository).findById(listId);
        }
    }

    @Nested
    @DisplayName("アイテムリスト作成のテスト")
    class CreateItemListTests {

        @Test
        @DisplayName("正常系：リクエストでリスト作成")
        void createItemList_WithRequest_Success() {
            // Given
            CreateItemListRequest request = CreateItemListRequest.builder()
                    .name("新しいリスト")
                    .build();

            ItemList savedEntity = createMockItemList(UUID.randomUUID(), "新しいリスト");

            when(itemListRepository.save(any(ItemList.class)))
                    .thenReturn(savedEntity);

            // When
            ItemListResponse result = itemListService.createItemList(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("新しいリスト");
            assertThat(result.getId()).isNotNull();

            verify(itemListRepository).save(any(ItemList.class));
        }

        @Test
        @DisplayName("正常系：名前なしでリスト作成")
        void createItemList_WithoutRequest_Success() {
            // Given
            // サービスはリクエストがない場合、デフォルト名「新しいリスト」を使用する
            ItemList savedEntity = createMockItemList(UUID.randomUUID(), "新しいリスト");

            when(itemListRepository.save(any(ItemList.class)))
                    .thenReturn(savedEntity);

            // When
            ItemListResponse result = itemListService.createItemList();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("新しいリスト");
            assertThat(result.getId()).isNotNull();

            verify(itemListRepository).save(any(ItemList.class));
        }
    }

    @Nested
    @DisplayName("アイテムリスト更新のテスト")
    class UpdateItemListTests {

        @Test
        @DisplayName("正常系：リスト更新 - リストが存在する場合")
        void updateItemList_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            CreateItemListRequest request = CreateItemListRequest.builder()
                    .name("更新されたリスト")
                    .build();

            ItemList existingEntity = createMockItemList(listId, "古いリスト名");
            ItemList updatedEntity = createMockItemList(listId, "更新されたリスト");

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.of(existingEntity));
            when(itemListRepository.save(existingEntity))
                    .thenReturn(updatedEntity);

            // When
            Optional<ItemListResponse> result = itemListService.updateItemList(listId, request);

            // Then
            assertThat(result).isPresent();
            ItemListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("更新されたリスト");

            verify(itemListRepository).findById(listId);
            verify(itemListRepository).save(existingEntity);
        }

        @Test
        @DisplayName("異常系：リスト更新 - リストが存在しない場合")
        void updateItemList_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();
            CreateItemListRequest request = CreateItemListRequest.builder()
                    .name("更新されたリスト")
                    .build();

            when(itemListRepository.findById(listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ItemListResponse> result = itemListService.updateItemList(listId, request);

            // Then
            assertThat(result).isEmpty();

            verify(itemListRepository).findById(listId);
            verify(itemListRepository, never()).save(any(ItemList.class));
        }
    }

    @Nested
    @DisplayName("アイテムリスト削除のテスト")
    class DeleteItemListTests {

        @Test
        @DisplayName("正常系：リスト削除 - リストが存在する場合")
        void deleteItemList_Success() {
            // Given
            UUID listId = UUID.randomUUID();

            when(itemListRepository.existsById(listId))
                    .thenReturn(true);

            // When
            boolean result = itemListService.deleteItemList(listId);

            // Then
            assertThat(result).isTrue();

            verify(itemListRepository).existsById(listId);
            verify(itemListRepository).deleteById(listId);
        }

        @Test
        @DisplayName("異常系：リスト削除 - リストが存在しない場合")
        void deleteItemList_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();

            when(itemListRepository.existsById(listId))
                    .thenReturn(false);

            // When
            boolean result = itemListService.deleteItemList(listId);

            // Then
            assertThat(result).isFalse();

            verify(itemListRepository).existsById(listId);
            verify(itemListRepository, never()).deleteById(any(UUID.class));
        }
    }

    /**
     * テスト用のモックItemListエンティティ作成
     */
    private ItemList createMockItemList(UUID id, String name) {
        ItemList entity = new ItemList();
        entity.setId(id);
        entity.setName(name);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}