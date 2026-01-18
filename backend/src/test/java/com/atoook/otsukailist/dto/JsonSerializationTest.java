package com.atoook.otsukailist.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON シリアライゼーションのテスト
 * boolean フィールドが正しくシリアライズ・デシリアライズされることを確認
 */
class JsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
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

        ItemResponse response = ItemResponse.builder()
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
        assertThat(jsonNode.get("completedByMemberId").asText()).isEqualTo(completedByMemberId.toString());
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
        String json = """
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
        String json = """
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
        assertThat(request.getCompletedByMemberId()).isEqualTo(UUID.fromString("4aa8c874-708b-4f96-8658-3f4daff9c6ee"));
    }
}
