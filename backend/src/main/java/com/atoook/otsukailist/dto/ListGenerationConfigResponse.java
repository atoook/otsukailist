package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import com.atoook.otsukailist.model.GenerationConfigType;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ListGenerationConfigResponse {
  private UUID id;
  private UUID listId;
  private GenerationConfigType configType;
  private JsonNode configJson;
  private Instant createdAt;
  private Instant updatedAt;
}
