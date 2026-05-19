import { fetchListGenerationConfig, saveListGenerationConfig } from '@/api/listGenerationConfig';
import type { ListGenerationConfigResponse } from '@/api/listGenerationConfig';
import { setGeneratorKeyExcluded, type ListGenerationConfigBase } from '@/lib/generation/generationConfig';
import { findListTemplateConfigAdapterByGeneratorKey } from '@/lib/listTemplateConfigRegistry';
import type { ApiError, Item, MutationResponse, UUID } from '@/types/api';

export type TemplateConfigExclusionMutation = () => Promise<
  MutationResponse<ListGenerationConfigResponse<ListGenerationConfigBase>>
>;

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

export async function buildGeneratedItemExclusionConfigMutation(
  listId: UUID,
  item: Item
): Promise<TemplateConfigExclusionMutation | null> {
  if (!isGeneratedItemWithKey(item)) {
    return null;
  }

  const adapter = findListTemplateConfigAdapterByGeneratorKey(item.quantified.generatorKey);
  if (adapter == null) {
    return null;
  }

  let config: ListGenerationConfigBase;
  try {
    const res = await fetchListGenerationConfig<unknown>(listId, adapter.id);
    if (!adapter.isConfig(res.configJson)) {
      return null;
    }
    config = adapter.normalizeConfig(res.configJson);
  } catch (err: unknown) {
    if (isNotFoundError(err)) {
      return null;
    }
    throw err;
  }

  const nextConfig = setGeneratorKeyExcluded(config, item.quantified.generatorKey, true);
  return () => saveListGenerationConfig(listId, adapter.id, nextConfig);
}

function isNotFoundError(err: unknown): boolean {
  return err !== null && typeof err === 'object' && 'error' in err && (err as ApiError).error === 'not_found';
}
