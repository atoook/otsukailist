package com.atoook.otsukailist.generation;

import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.ItemCategory;

public interface GenerationRule {
  /** Returns the stable key used to match generated items. */
  String generatorKey();

  /** Returns the item category assigned by this generation rule. */
  ItemCategory category();

  /** Returns the canonical unit stored for generated quantities. */
  BaseUnit baseUnit();

  /** Returns the preferred display unit for generated quantities. */
  String displayUnit();
}
