package com.atoook.otsukailist.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atoook.otsukailist.dto.ListGenerationConfigRequest;
import com.atoook.otsukailist.dto.ListGenerationConfigResponse;
import com.atoook.otsukailist.dto.MutationResponse;
import com.atoook.otsukailist.service.ListGenerationConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lists/{listId}/generation-configs")
public class ListGenerationConfigController {

  private final ListGenerationConfigService listGenerationConfigService;

  /**
   * Get the saved generation config for the specified list and type.
   *
   * @param listId target list ID
   * @param configType generation config type value
   * @return saved generation config
   */
  @GetMapping("/{configType}")
  public ResponseEntity<ListGenerationConfigResponse> getConfig(
      @PathVariable("listId") UUID listId, @PathVariable("configType") String configType) {
    return ResponseEntity.ok(listGenerationConfigService.getConfig(listId, configType));
  }

  /**
   * Create or update the generation config for the specified list and type.
   *
   * @param listId target list ID
   * @param configType generation config type value
   * @param req generation config payload
   * @return saved generation config with latest list revision
   */
  @PutMapping("/{configType}")
  public ResponseEntity<MutationResponse<ListGenerationConfigResponse>> upsertConfig(
      @PathVariable("listId") UUID listId,
      @PathVariable("configType") String configType,
      @Valid @RequestBody ListGenerationConfigRequest req) {
    return ResponseEntity.ok(listGenerationConfigService.upsertConfig(listId, configType, req));
  }
}
