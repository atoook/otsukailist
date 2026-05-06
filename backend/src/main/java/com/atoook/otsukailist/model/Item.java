package com.atoook.otsukailist.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "item")
public class Item {
  @Id
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "item_type", nullable = false, length = 20)
  private ItemType itemType = ItemType.PLAIN;

  @Column(name = "category", columnDefinition = "text")
  private ItemCategory category;

  @Column(name = "preparation_type", columnDefinition = "text")
  private ItemPreparationType preparationType;

  // DB物理名: is_completed（TINYINT(1)）
  @Column(name = "is_completed", nullable = false)
  private boolean completed;

  // 担当者（member.id）。未アサインのときはNULL。
  @Column(name = "assigned_member_id")
  private UUID assignedMemberId;

  // 誰が完了させたか（member.id）。未完了のときはNULL。
  @Column(name = "completed_by_member_id")
  private UUID completedByMemberId;

  // いつ完了させたか。未完了のときはNULL。
  @Column(name = "completed_at")
  private Instant completedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "list_id", nullable = false)
  private ItemList itemList;

  @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private ItemQuantified quantified;

  public void setQuantified(ItemQuantified quantified) {
    if (this.quantified != null) {
      this.quantified.setItem(null);
    }
    this.quantified = quantified;
    if (quantified != null) {
      quantified.setItem(this);
    }
  }
}
