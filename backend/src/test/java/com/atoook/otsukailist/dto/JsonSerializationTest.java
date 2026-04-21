package com.atoook.otsukailist.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JSON シリアライゼーションのテスト boolean フィールドが正しくシリアライズ・デシリアライズされることを確認 */
class JsonSerializationTest {

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  @DisplayName("ItemResponse の JSON シリアライゼーション テスト")
  void testItemResponseSerialization() throws JsonProcessingException {
    // Given
    UUID id = UUID.randomUUID();
    UUID completedByMemberId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2024-01-01T01:00:00Z");
    Instant completedAt = Instant.parse("2024-01-01T00:30:00Z");

    ItemResponse response =
        ItemResponse.builder()
            .id(id)
            .name("テストアイテム")
            .completed(true)
            .completedByMemberId(completedByMemberId)
            .completedAt(completedAt)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    // When - シリアライゼーション
    String json = objectMapper.writeValueAsString(response);

    // Then - JSON に "completed" フィールドが含まれることを確認
    JsonNode jsonNode = objectMapper.readTree(json);
    assertThat(jsonNode.get("id").asText()).isEqualTo(id.toString());
    assertThat(jsonNode.get("completed").asBoolean()).isTrue();
    assertThat(jsonNode.get("completedByMemberId").asText())
        .isEqualTo(completedByMemberId.toString());
    assertThat(jsonNode.get("completedAt").asText()).isEqualTo(completedAt.toString());
    assertThat(jsonNode.get("createdAt").asText()).isEqualTo(createdAt.toString());
    assertThat(jsonNode.get("updatedAt").asText()).isEqualTo(updatedAt.toString());

    // デシリアライゼーション
    ItemResponse deserialized = objectMapper.readValue(json, ItemResponse.class);
    assertThat(deserialized.getId()).isEqualTo(id);
    assertThat(deserialized.isCompleted()).isTrue();
    assertThat(deserialized.getCompletedByMemberId()).isEqualTo(completedByMemberId);
    assertThat(deserialized.getCompletedAt()).isEqualTo(completedAt);
    assertThat(deserialized.getCreatedAt()).isEqualTo(createdAt);
    assertThat(deserialized.getUpdatedAt()).isEqualTo(updatedAt);
    assertThat(deserialized.getName()).isEqualTo("テストアイテム");
  }

  @Test
  @DisplayName("CreateItemRequest の JSON デシリアライゼーション テスト")
  void testCreateItemRequestDeserialization() throws JsonProcessingException {
    // Given
    String json =
        """
                {
                    "name": "新しいアイテム",
                    "completed": false
                }
                """;

    // When
    CreateItemRequest request = objectMapper.readValue(json, CreateItemRequest.class);

    // Then
    assertThat(request.getName()).isEqualTo("新しいアイテム");
    assertThat(request.isCompleted()).isFalse();
  }

  @Test
  @DisplayName("UpdateItemRequest の JSON デシリアライゼーション テスト")
  void testUpdateItemRequestDeserialization() throws JsonProcessingException {
    // Given
    String json =
        """
                {
                    "name": "更新されたアイテム",
                    "completed": true,
                    "completedByMemberId": "4aa8c874-708b-4f96-8658-3f4daff9c6ee"
                }
                """;

    // When
    UpdateItemRequest request = objectMapper.readValue(json, UpdateItemRequest.class);

    // Then
    assertThat(request.getName()).isEqualTo("更新されたアイテム");
    assertThat(request.getCompleted()).isTrue();
    assertThat(request.getCompletedByMemberId())
        .isEqualTo(UUID.fromString("4aa8c874-708b-4f96-8658-3f4daff9c6ee"));
  }

  @Test
  @DisplayName("ItemListSnapshotResponse の lastItemActivityAt が ISO 8601 文字列でシリアライズされること")
  void testItemListSnapshotResponseSerializationWithLastItemActivityAt()
      throws JsonProcessingException {
    // Given
    UUID listId = UUID.randomUUID();
    Instant lastItemActivityAt = Instant.parse("2024-06-15T10:30:00Z");

    ItemListSnapshotResponse response =
        ItemListSnapshotResponse.builder()
            .listId(listId)
            .name("テストリスト")
            .revision(1L)
            .itemCount(3)
            .serverTime(Instant.parse("2024-06-15T11:00:00Z"))
            .lastItemActivityAt(lastItemActivityAt)
            .build();

    // When
    String json = objectMapper.writeValueAsString(response);

    // Then
    JsonNode jsonNode = objectMapper.readTree(json);
    assertThat(jsonNode.get("lastItemActivityAt").asText())
        .isEqualTo(lastItemActivityAt.toString());
  }

  @Test
  @DisplayName("ItemListSnapshotResponse の lastItemActivityAt が null のとき JSON null でシリアライズされること")
  void testItemListSnapshotResponseSerializationWithNullLastItemActivityAt()
      throws JsonProcessingException {
    // Given
    UUID listId = UUID.randomUUID();

    ItemListSnapshotResponse response =
        ItemListSnapshotResponse.builder()
            .listId(listId)
            .name("テストリスト")
            .revision(0L)
            .itemCount(0)
            .serverTime(Instant.parse("2024-06-15T11:00:00Z"))
            .lastItemActivityAt(null)
            .build();

    // When
    String json = objectMapper.writeValueAsString(response);

    // Then
    JsonNode jsonNode = objectMapper.readTree(json);
    assertThat(jsonNode.has("lastItemActivityAt")).isTrue();
    assertThat(jsonNode.get("lastItemActivityAt").isNull()).isTrue();
  }

  @Test
  @DisplayName("ListMetaItemResponse の lastItemActivityAt が ISO 8601 文字列でシリアライズされること")
  void testListMetaItemResponseSerializationWithLastItemActivityAt()
      throws JsonProcessingException {
    // Given
    UUID listId = UUID.randomUUID();
    Instant lastItemActivityAt = Instant.parse("2024-06-15T10:30:00Z");

    ListMetaItemResponse response =
        ListMetaItemResponse.builder()
            .listId(listId)
            .name("テストリスト")
            .itemCount(5L)
            .incompleteCount(2L)
            .lastItemActivityAt(lastItemActivityAt)
            .build();

    // When
    String json = objectMapper.writeValueAsString(response);

    // Then
    JsonNode jsonNode = objectMapper.readTree(json);
    assertThat(jsonNode.get("lastItemActivityAt").asText())
        .isEqualTo(lastItemActivityAt.toString());
    assertThat(jsonNode.get("itemCount").asLong()).isEqualTo(5L);
    assertThat(jsonNode.get("incompleteCount").asLong()).isEqualTo(2L);
  }

  @Test
  @DisplayName("ListMetaItemResponse の lastItemActivityAt が null のとき JSON null でシリアライズされること")
  void testListMetaItemResponseSerializationWithNullLastItemActivityAt()
      throws JsonProcessingException {
    // Given
    ListMetaItemResponse response =
        ListMetaItemResponse.builder()
            .listId(UUID.randomUUID())
            .name("空のリスト")
            .itemCount(0L)
            .incompleteCount(0L)
            .lastItemActivityAt(null)
            .build();

    // When
    String json = objectMapper.writeValueAsString(response);

    // Then
    JsonNode jsonNode = objectMapper.readTree(json);
    assertThat(jsonNode.has("lastItemActivityAt")).isTrue();
    assertThat(jsonNode.get("lastItemActivityAt").isNull()).isTrue();
  }
}
