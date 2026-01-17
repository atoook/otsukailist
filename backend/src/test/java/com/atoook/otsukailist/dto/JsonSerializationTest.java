package com.atoook.otsukailist.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON シリアライゼーションのテスト
 * boolean フィールドが正しくシリアライズ・デシリアライズされることを確認
 */
class JsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("ItemResponse の JSON シリアライゼーション テスト")
    void testItemResponseSerialization() throws JsonProcessingException {
        // Given
        ItemResponse response = ItemResponse.builder()
                .id(UUID.randomUUID())
                .name("テストアイテム")
                .completed(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .listId(UUID.randomUUID())
                .build();

        // When - シリアライゼーション
        String json = objectMapper.writeValueAsString(response);

        // Then - JSON に "completed" フィールドが含まれることを確認
        JsonNode jsonNode = objectMapper.readTree(json);
        assertThat(jsonNode.has("completed")).isTrue();
        assertThat(jsonNode.get("completed").asBoolean()).isTrue();

        // デシリアライゼーション
        ItemResponse deserialized = objectMapper.readValue(json, ItemResponse.class);
        assertThat(deserialized.isCompleted()).isTrue();
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
                    "completed": true
                }
                """;

        // When
        UpdateItemRequest request = objectMapper.readValue(json, UpdateItemRequest.class);

        // Then
        assertThat(request.getName()).isEqualTo("更新されたアイテム");
        assertThat(request.getCompleted()).isTrue();
    }
}
