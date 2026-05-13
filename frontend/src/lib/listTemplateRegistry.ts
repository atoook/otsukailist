import { fetchListGenerationConfig, type ListGenerationConfigType } from '@/api/listGenerationConfig';
import { isBBQGenerationConfig } from '@/lib/generation/bbqGenerator';
import type { ApiError, UUID } from '@/types/api';

export type ListTemplateDefinition = {
  id: ListGenerationConfigType;
  routeName: string;
  createLabel: string;
  updateLabel: string;
  description: string;
  hasConfig(listId: UUID): Promise<boolean>;
};

function isNotFoundError(err: unknown): boolean {
  return err !== null && typeof err === 'object' && 'error' in err && (err as ApiError).error === 'not_found';
}

const BBQ_LIST_TEMPLATE: ListTemplateDefinition = {
  id: 'bbq',
  routeName: 'BBQGeneration',
  createLabel: 'テンプレートから追加',
  updateLabel: '生成条件を調整',
  description: 'BBQなどの条件からまとめて候補を作成',
  async hasConfig(listId: UUID): Promise<boolean> {
    try {
      const res = await fetchListGenerationConfig<unknown>(listId, 'bbq');
      return isBBQGenerationConfig(res.configJson);
    } catch (err: unknown) {
      if (isNotFoundError(err)) {
        return false;
      }
      throw err;
    }
  }
};

export const LIST_TEMPLATES: readonly ListTemplateDefinition[] = [BBQ_LIST_TEMPLATE];

export const PRIMARY_LIST_TEMPLATE: ListTemplateDefinition = BBQ_LIST_TEMPLATE;
