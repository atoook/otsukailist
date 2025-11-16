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

import com.atoook.otsukailist.dto.CreateShoppingListRequest;
import com.atoook.otsukailist.dto.ShoppingListResponse;
import com.atoook.otsukailist.model.ShoppingList;
import com.atoook.otsukailist.repository.ShoppingListRepository;

/**
 * ShoppingListService のユニットテスト
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Nested
    @DisplayName("ショッピングリスト取得のテスト")
    class GetShoppingListTests {

        @Test
        @DisplayName("正常系：IDで取得 - リストが存在する場合")
        void getShoppingListById_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            ShoppingList mockEntity = createMockShoppingList(listId, "テストリスト");

            when(shoppingListRepository.findById(listId))
                    .thenReturn(Optional.of(mockEntity));

            // When
            Optional<ShoppingListResponse> result = shoppingListService.getShoppingListById(listId);

            // Then
            assertThat(result).isPresent();
            ShoppingListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("テストリスト");

            verify(shoppingListRepository).findById(listId);
        }

        @Test
        @DisplayName("異常系：IDで取得 - リストが存在しない場合")
        void getShoppingListById_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();

            when(shoppingListRepository.findById(listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ShoppingListResponse> result = shoppingListService.getShoppingListById(listId);

            // Then
            assertThat(result).isEmpty();
            verify(shoppingListRepository).findById(listId);
        }

        @Test
        @DisplayName("正常系：アイテム詳細含む取得 - リストが存在する場合")
        void getShoppingListByIdWithItems_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            ShoppingList mockEntity = createMockShoppingList(listId, "テストリスト");

            when(shoppingListRepository.findById(listId))
                    .thenReturn(Optional.of(mockEntity));

            // When
            Optional<ShoppingListResponse> result = shoppingListService.getShoppingListByIdWithItems(listId);

            // Then
            assertThat(result).isPresent();
            ShoppingListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("テストリスト");

            verify(shoppingListRepository).findById(listId);
        }
    }

    @Nested
    @DisplayName("ショッピングリスト作成のテスト")
    class CreateShoppingListTests {

        @Test
        @DisplayName("正常系：リクエストでリスト作成")
        void createShoppingList_WithRequest_Success() {
            // Given
            CreateShoppingListRequest request = CreateShoppingListRequest.builder()
                    .name("新しいリスト")
                    .build();

            ShoppingList savedEntity = createMockShoppingList(UUID.randomUUID(), "新しいリスト");

            when(shoppingListRepository.save(any(ShoppingList.class)))
                    .thenReturn(savedEntity);

            // When
            ShoppingListResponse result = shoppingListService.createShoppingList(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("新しいリスト");
            assertThat(result.getId()).isNotNull();

            verify(shoppingListRepository).save(any(ShoppingList.class));
        }

        @Test
        @DisplayName("正常系：名前なしでリスト作成")
        void createShoppingList_WithoutRequest_Success() {
            // Given
            ShoppingList savedEntity = createMockShoppingList(UUID.randomUUID(), "新しいリスト");

            when(shoppingListRepository.save(any(ShoppingList.class)))
                    .thenReturn(savedEntity);

            // When
            ShoppingListResponse result = shoppingListService.createShoppingList();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("新しいリスト");
            assertThat(result.getId()).isNotNull();

            verify(shoppingListRepository).save(any(ShoppingList.class));
        }
    }

    @Nested
    @DisplayName("ショッピングリスト更新のテスト")
    class UpdateShoppingListTests {

        @Test
        @DisplayName("正常系：リスト更新 - リストが存在する場合")
        void updateShoppingList_Success() {
            // Given
            UUID listId = UUID.randomUUID();
            CreateShoppingListRequest request = CreateShoppingListRequest.builder()
                    .name("更新されたリスト")
                    .build();

            ShoppingList existingEntity = createMockShoppingList(listId, "古いリスト名");
            ShoppingList updatedEntity = createMockShoppingList(listId, "更新されたリスト");

            when(shoppingListRepository.findById(listId))
                    .thenReturn(Optional.of(existingEntity));
            when(shoppingListRepository.save(existingEntity))
                    .thenReturn(updatedEntity);

            // When
            Optional<ShoppingListResponse> result = shoppingListService.updateShoppingList(listId, request);

            // Then
            assertThat(result).isPresent();
            ShoppingListResponse response = result.get();
            assertThat(response.getId()).isEqualTo(listId);
            assertThat(response.getName()).isEqualTo("更新されたリスト");

            verify(shoppingListRepository).findById(listId);
            verify(shoppingListRepository).save(existingEntity);
        }

        @Test
        @DisplayName("異常系：リスト更新 - リストが存在しない場合")
        void updateShoppingList_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();
            CreateShoppingListRequest request = CreateShoppingListRequest.builder()
                    .name("更新されたリスト")
                    .build();

            when(shoppingListRepository.findById(listId))
                    .thenReturn(Optional.empty());

            // When
            Optional<ShoppingListResponse> result = shoppingListService.updateShoppingList(listId, request);

            // Then
            assertThat(result).isEmpty();

            verify(shoppingListRepository).findById(listId);
            verify(shoppingListRepository, never()).save(any(ShoppingList.class));
        }
    }

    @Nested
    @DisplayName("ショッピングリスト削除のテスト")
    class DeleteShoppingListTests {

        @Test
        @DisplayName("正常系：リスト削除 - リストが存在する場合")
        void deleteShoppingList_Success() {
            // Given
            UUID listId = UUID.randomUUID();

            when(shoppingListRepository.existsById(listId))
                    .thenReturn(true);

            // When
            boolean result = shoppingListService.deleteShoppingList(listId);

            // Then
            assertThat(result).isTrue();

            verify(shoppingListRepository).existsById(listId);
            verify(shoppingListRepository).deleteById(listId);
        }

        @Test
        @DisplayName("異常系：リスト削除 - リストが存在しない場合")
        void deleteShoppingList_NotFound() {
            // Given
            UUID listId = UUID.randomUUID();

            when(shoppingListRepository.existsById(listId))
                    .thenReturn(false);

            // When
            boolean result = shoppingListService.deleteShoppingList(listId);

            // Then
            assertThat(result).isFalse();

            verify(shoppingListRepository).existsById(listId);
            verify(shoppingListRepository, never()).deleteById(any(UUID.class));
        }
    }

    /**
     * テスト用のモックShoppingListエンティティ作成
     */
    private ShoppingList createMockShoppingList(UUID id, String name) {
        ShoppingList entity = new ShoppingList();
        entity.setId(id);
        entity.setName(name);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}