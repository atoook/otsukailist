package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atoook.otsukailist.model.ItemQuantified;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;

public interface ItemQuantifiedRepository extends JpaRepository<ItemQuantified, UUID> {
  List<ItemQuantified> findByItemItemListIdAndOriginAndRegenerationPolicy(
      UUID listId, Origin origin, RegenerationPolicy regenerationPolicy);
}
