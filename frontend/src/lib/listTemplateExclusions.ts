import { fetchListGenerationConfig, saveListGenerationConfig } from '@/api/listGenerationConfig';
import type { ListGenerationConfigResponse } from '@/api/listGenerationConfig';
import {
  isBBQGenerationConfig,
  normalizeBBQGenerationConfig,
  type BBQGenerationConfig
} from '@/lib/generation/bbqGenerator';
import { setGeneratorKeyExcluded } from '@/lib/generation/generationConfig';
import type { ApiError, Item, MutationResponse, UUID } from '@/types/api';

type GeneratedItemWithKey = Item & {
  quantified: NonNullable<Item['quantified']> & { generatorKey: string };
};

export function isGeneratedItemWithKey(item: Item): item is GeneratedItemWithKey {
  return (
    item.itemType === 'quantified' &&
    item.quantified != null &&
    item.quantified.origin === 'generated' &&
    typeof item.quantified.generatorKey === 'string' &&
    item.quantified.generatorKey.length > 0
  );
}

export async function markGeneratedItemExcludedFromBBQConfig(
  listId: UUID,
  item: Item
): Promise<MutationResponse<ListGenerationConfigResponse<BBQGenerationConfig>> | null> {
  if (!isGeneratedItemWithKey(item)) {
    return null;
  }

  let config: BBQGenerationConfig;
  try {
    const res = await fetchListGenerationConfig<unknown>(listId, 'bbq');
    if (!isBBQGenerationConfig(res.configJson)) {
      return null;
    }
    config = normalizeBBQGenerationConfig(res.configJson);
  } catch (err: unknown) {
    if (isNotFoundError(err)) {
      return null;
    }
    throw err;
  }

  const nextConfig = setGeneratorKeyExcluded(config, item.quantified.generatorKey, true);
  return saveListGenerationConfig(listId, 'bbq', nextConfig);
}

function isNotFoundError(err: unknown): boolean {
  return err !== null && typeof err === 'object' && 'error' in err && (err as ApiError).error === 'not_found';
}
