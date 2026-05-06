package com.atoook.otsukailist.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atoook.otsukailist.model.GenerationConfigType;
import com.atoook.otsukailist.model.ListGenerationConfig;

public interface ListGenerationConfigRepository extends JpaRepository<ListGenerationConfig, UUID> {
  Optional<ListGenerationConfig> findByItemListIdAndConfigType(
      UUID itemListId, GenerationConfigType configType);
}
