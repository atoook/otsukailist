export type ListGenerationConfigBase = {
  version: number;
  excludedGeneratorKeys?: string[];
};

export function normalizeExcludedGeneratorKeys(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return [
    ...new Set(
      value
        .filter((key): key is string => typeof key === 'string')
        .map((key) => key.trim())
        .filter((key) => key.length > 0)
    )
  ];
}

export function isExcludedGeneratorKeys(value: unknown): value is string[] | undefined {
  return value === undefined || (Array.isArray(value) && value.every((key) => typeof key === 'string'));
}

export function isGeneratorKeyExcluded(config: ListGenerationConfigBase, generatorKey: string): boolean {
  return normalizeExcludedGeneratorKeys(config.excludedGeneratorKeys).includes(generatorKey);
}

export function setGeneratorKeyExcluded<T extends ListGenerationConfigBase>(
  config: T,
  generatorKey: string,
  excluded: boolean
): T {
  const keys = new Set(normalizeExcludedGeneratorKeys(config.excludedGeneratorKeys));
  if (excluded) {
    keys.add(generatorKey);
  } else {
    keys.delete(generatorKey);
  }

  return {
    ...config,
    excludedGeneratorKeys: [...keys]
  };
}
