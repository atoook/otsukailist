import type { ListGenerationConfigType } from '@/api/listGenerationConfig';
import {
  isBBQGenerationConfig,
  normalizeBBQGenerationConfig,
  type BBQGenerationConfig
} from '@/lib/generation/bbqGenerator';
import type { ListGenerationConfigBase } from '@/lib/generation/generationConfig';
import { BBQ_GENERATOR_KEYS } from '@/lib/listGenerationConstants';

export type ListTemplateConfigAdapter<TConfig extends ListGenerationConfigBase = ListGenerationConfigBase> = {
  id: ListGenerationConfigType;
  generatorKeys: readonly string[];
  isConfig(value: unknown): value is TConfig;
  normalizeConfig(config: TConfig): TConfig;
};

const BBQ_TEMPLATE_CONFIG_ADAPTER: ListTemplateConfigAdapter<BBQGenerationConfig> = {
  id: 'bbq',
  generatorKeys: BBQ_GENERATOR_KEYS,
  isConfig: isBBQGenerationConfig,
  normalizeConfig: normalizeBBQGenerationConfig
};

export const LIST_TEMPLATE_CONFIG_ADAPTERS: readonly ListTemplateConfigAdapter[] = [BBQ_TEMPLATE_CONFIG_ADAPTER];

export function findListTemplateConfigAdapterByGeneratorKey(generatorKey: string): ListTemplateConfigAdapter | null {
  return LIST_TEMPLATE_CONFIG_ADAPTERS.find((adapter) => adapter.generatorKeys.includes(generatorKey)) ?? null;
}
