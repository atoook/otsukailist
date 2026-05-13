package com.atoook.otsukailist.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.dto.ListGenerationConfigRequest;
import com.atoook.otsukailist.dto.ListGenerationConfigResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.model.GenerationConfigType;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ListGenerationConfig;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ListGenerationConfigRepository;
import com.atoook.otsukailist.service.message.ErrorMessages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListGenerationConfigService {

  private static final String MSG_CONFIG_TYPE_UNKNOWN = "未知の生成設定タイプです";
  private static final String MSG_CONFIG_JSON_INVALID = "生成条件の形式が不正です";

  private final ItemListRepository itemListRepo;
  private final ListGenerationConfigRepository listGenerationConfigRepo;
  private final ListRevisionService listRevisionService;
  private final ObjectMapper objectMapper;

  /**
   * Get the saved generation config for a list.
   *
   * @param listId target list ID
   * @param configTypeValue generation config type value
   * @return saved generation config
   */
  @Transactional(readOnly = true)
  public ListGenerationConfigResponse getConfig(UUID listId, String configTypeValue) {
    GenerationConfigType configType = resolveConfigType(configTypeValue);
    ensureListExists(listId);

    ListGenerationConfig config =
        listGenerationConfigRepo
            .findByItemListIdAndConfigType(listId, configType)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "生成設定")));

    return toResponse(config);
  }

  /**
   * Create or update the generation config for a list.
   *
   * @param listId target list ID
   * @param configTypeValue generation config type value
   * @param req generation config payload
   * @return saved generation config with latest list revision
   */
  @Transactional
  public MutationResponse<ListGenerationConfigResponse> upsertConfig(
      UUID listId, String configTypeValue, ListGenerationConfigRequest req) {
    GenerationConfigType configType = resolveConfigType(configTypeValue);
    ItemList list = ensureListExists(listId);

    ListGenerationConfig config =
        listGenerationConfigRepo
            .findByItemListIdAndConfigType(listId, configType)
            .orElseGet(ListGenerationConfig::new);

    config.setItemList(list);
    config.setConfigType(configType);
    config.setConfigJson(writeConfigJson(req.getConfigJson()));

    ListGenerationConfig saved = listGenerationConfigRepo.save(config);
    long revision = listRevisionService.incrementAndGet(listId);

    return MutationResponse.<ListGenerationConfigResponse>builder()
        .revision(revision)
        .data(toResponse(saved))
        .build();
  }

  private ItemList ensureListExists(UUID listId) {
    return itemListRepo
        .findById(listId)
        .orElseThrow(
            () -> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND, "リスト")));
  }

  private static GenerationConfigType resolveConfigType(String configTypeValue) {
    try {
      return GenerationConfigType.fromValue(configTypeValue);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(MSG_CONFIG_TYPE_UNKNOWN, e);
    }
  }

  private String writeConfigJson(JsonNode configJson) {
    try {
      return objectMapper.writeValueAsString(configJson);
    } catch (JsonProcessingException e) {
      throw new BadRequestException(MSG_CONFIG_JSON_INVALID, e);
    }
  }

  private JsonNode readConfigJson(String configJson) {
    try {
      return objectMapper.readTree(configJson);
    } catch (JsonProcessingException e) {
      throw new BadRequestException(MSG_CONFIG_JSON_INVALID, e);
    }
  }

  private ListGenerationConfigResponse toResponse(ListGenerationConfig config) {
    return ListGenerationConfigResponse.builder()
        .id(config.getId())
        .listId(config.getItemList().getId())
        .configType(config.getConfigType())
        .configJson(readConfigJson(config.getConfigJson()))
        .createdAt(config.getCreatedAt())
        .updatedAt(config.getUpdatedAt())
        .build();
  }
}
