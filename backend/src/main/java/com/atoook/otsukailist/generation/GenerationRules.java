package com.atoook.otsukailist.generation;

import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GenerationRules {
  public static final Map<String, GenerationRule> VALUES = Map.copyOf(BBQGenerationRules.VALUES);

  /** Finds a generation rule by its stable generator key. */
  public static GenerationRule findByGeneratorKey(String generatorKey) {
    return VALUES.get(generatorKey);
  }

  /** Returns whether the given generator key is registered. */
  public static boolean containsGeneratorKey(String generatorKey) {
    return VALUES.containsKey(generatorKey);
  }
}
