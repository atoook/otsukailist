package com.atoook.otsukailist.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.SyncGeneratedItemsRequest;
import com.atoook.otsukailist.dto.SyncGeneratedItemsResponse;
import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.generation.GenerationRule;
import com.atoook.otsukailist.generation.GenerationRules;
import com.atoook.otsukailist.model.ItemType;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;
import com.atoook.otsukailist.service.GeneratedItemSyncCommandService.GeneratedItemUpdateCommand;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneratedItemCommandService {

  private static final String MSG_GENERATED_ITEM_REQUIRED = "生成アイテムの形式が不正です";
  private static final String MSG_GENERATOR_KEY_REQUIRED = "自動生成アイテムには生成ルールIDが必須です";
  private static final String MSG_GENERATOR_KEY_UNKNOWN = "未知の生成ルールIDです";
  private static final String MSG_GENERATOR_KEY_DUPLICATED = "生成ルールIDが重複しています";
  private static final String MSG_GENERATOR_KEY_SCOPE_REQUIRED = "生成ルールID一覧が不正です";
  private static final String MSG_ITEM_NAME_REQUIRED = "アイテム名は必須です";

  private final GeneratedItemSyncCommandService generatedItemSyncCommandService;

  /**
   * Validate and sync generated item candidates without locking existing generated-auto items.
   *
   * @param listId target list ID
   * @param req generated item candidates and generator-key scope
   * @return synced items with latest list revision
   */
  public MutationResponse<SyncGeneratedItemsResponse> syncGeneratedItems(
      UUID listId, SyncGeneratedItemsRequest req) {
    List<String> generatorKeysInScope = validateGeneratorKeysInScope(req.getGeneratorKeysInScope());
    List<CreateItemRequest> requests = req.getItems() == null ? List.of() : req.getItems();
    List<GeneratedItemUpdateCommand> commands = validateAndBuildCommands(requests);
    validateCommandKeysInScope(commands, generatorKeysInScope);
    return generatedItemSyncCommandService.syncGeneratedItems(
        listId, commands, generatorKeysInScope);
  }

  private static List<String> validateGeneratorKeysInScope(List<String> generatorKeysInScope) {
    if (generatorKeysInScope == null || generatorKeysInScope.isEmpty()) {
      throw new BadRequestException(MSG_GENERATOR_KEY_SCOPE_REQUIRED);
    }
    Set<String> uniqueKeys = new HashSet<>();
    List<String> normalizedKeys = new ArrayList<>();
    for (String key : generatorKeysInScope) {
      String generatorKey = normalizeNullableText(key);
      if (generatorKey == null || !GenerationRules.containsGeneratorKey(generatorKey)) {
        throw new BadRequestException(MSG_GENERATOR_KEY_UNKNOWN);
      }
      if (!uniqueKeys.add(generatorKey)) {
        throw new BadRequestException(MSG_GENERATOR_KEY_DUPLICATED);
      }
      normalizedKeys.add(generatorKey);
    }
    return normalizedKeys;
  }

  private static void validateCommandKeysInScope(
      List<GeneratedItemUpdateCommand> commands, List<String> generatorKeysInScope) {
    Set<String> scopeKeys = new HashSet<>(generatorKeysInScope);
    for (GeneratedItemUpdateCommand command : commands) {
      if (!scopeKeys.contains(command.generatorKey())) {
        throw new BadRequestException(MSG_GENERATOR_KEY_SCOPE_REQUIRED);
      }
    }
  }

  private static List<GeneratedItemUpdateCommand> validateAndBuildCommands(
      List<CreateItemRequest> requests) {
    Set<String> generatorKeys = new HashSet<>();
    List<GeneratedItemUpdateCommand> commands = new ArrayList<>();
    for (CreateItemRequest request : requests) {
      validateGeneratedItemRequest(request);
      String generatorKey = request.getQuantified().getGeneratorKey().trim();
      if (!generatorKeys.add(generatorKey)) {
        throw new BadRequestException(MSG_GENERATOR_KEY_DUPLICATED);
      }
      commands.add(toGeneratedItemUpdateCommand(request, generatorKey));
    }
    return commands;
  }

  private static GeneratedItemUpdateCommand toGeneratedItemUpdateCommand(
      CreateItemRequest request, String generatorKey) {
    QuantifiedItemRequest quantifiedRequest = request.getQuantified();
    GenerationRule rule = GenerationRules.findByGeneratorKey(generatorKey);
    return new GeneratedItemUpdateCommand(
        normalizeRequiredText(request.getName(), MSG_ITEM_NAME_REQUIRED),
        rule.category(),
        request.getPreparationType(),
        request.getAssignedMemberId(),
        QuantifiedItemRequest.builder()
            .quantity(quantifiedRequest.getQuantity())
            .baseUnit(quantifiedRequest.getBaseUnit())
            .origin(Origin.GENERATED)
            .regenerationPolicy(RegenerationPolicy.AUTO)
            .generatorKey(generatorKey)
            .build());
  }

  private static void validateGeneratedItemRequest(CreateItemRequest request) {
    if (!isGeneratedAutoQuantifiedRequest(request)) {
      throw new BadRequestException(MSG_GENERATED_ITEM_REQUIRED);
    }

    validateGeneratorKey(request.getQuantified());
  }

  private static boolean isGeneratedAutoQuantifiedRequest(CreateItemRequest request) {
    return request != null
        && request.getItemType() == ItemType.QUANTIFIED
        && request.getQuantified() != null
        && request.getQuantified().getOrigin() == Origin.GENERATED
        && request.getQuantified().getRegenerationPolicy() == RegenerationPolicy.AUTO;
  }

  private static void validateGeneratorKey(QuantifiedItemRequest quantified) {
    String generatorKey = normalizeNullableText(quantified.getGeneratorKey());
    if (generatorKey == null) {
      throw new BadRequestException(MSG_GENERATOR_KEY_REQUIRED);
    }
    if (!GenerationRules.containsGeneratorKey(generatorKey)) {
      throw new BadRequestException(MSG_GENERATOR_KEY_UNKNOWN);
    }
    if (!Objects.equals(
        quantified.getBaseUnit(), GenerationRules.findByGeneratorKey(generatorKey).baseUnit())) {
      throw new BadRequestException(MSG_GENERATED_ITEM_REQUIRED);
    }
  }

  private static String normalizeNullableText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static String normalizeRequiredText(String value, String message) {
    String normalizedValue = normalizeNullableText(value);
    if (normalizedValue == null) {
      throw new BadRequestException(message);
    }
    return normalizedValue;
  }
}
