package com.atoook.otsukailist.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "item_quantified")
public class ItemQuantified {
  @Id
  @Column(name = "item_id", nullable = false, updatable = false)
  private UUID itemId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "quantity", nullable = false)
  private long quantity;

  @Column(name = "base_unit", nullable = false, length = 20)
  private BaseUnit baseUnit;

  @Column(name = "origin", nullable = false, length = 20)
  private Origin origin;

  @Column(name = "regeneration_policy", nullable = false, length = 20)
  private RegenerationPolicy regenerationPolicy;

  @Column(name = "generator_key", length = 80)
  private String generatorKey;
}
