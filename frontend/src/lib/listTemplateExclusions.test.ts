import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchListGenerationConfig, saveListGenerationConfig } from '@/api/listGenerationConfig';
import { createDefaultBBQGenerationConfig } from '@/lib/generation/bbqGenerator';
import { buildGeneratedItemExclusionConfigMutation } from '@/lib/listTemplateExclusions';
import type { Item } from '@/types/api';

vi.mock('@/api/listGenerationConfig', () => ({
  fetchListGenerationConfig: vi.fn(),
  saveListGenerationConfig: vi.fn()
}));

const mockedFetchListGenerationConfig = vi.mocked(fetchListGenerationConfig);
const mockedSaveListGenerationConfig = vi.mocked(saveListGenerationConfig);

function generatedItem(generatorKey: string): Item {
  return {
    id: `item-${generatorKey}`,
    name: generatorKey,
    itemType: 'quantified',
    category: 'meat',
    preparationType: null,
    quantified: {
      quantity: 100,
      baseUnit: 'g',
      origin: 'generated',
      regenerationPolicy: 'auto',
      generatorKey
    },
    completed: false,
    assignedMemberId: null,
    completedByMemberId: null,
    completedAt: null,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  };
}

function plainItem(): Item {
  return {
    ...generatedItem('beef'),
    itemType: 'plain',
    quantified: null
  };
}

describe('buildGeneratedItemExclusionConfigMutation', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('generated item の generatorKey を除外した config 保存 mutation を作る', async () => {
    const config = createDefaultBBQGenerationConfig();
    mockedFetchListGenerationConfig.mockResolvedValue({
      id: 'config-id',
      listId: 'list-id',
      configType: 'bbq',
      configJson: config,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z'
    });
    mockedSaveListGenerationConfig.mockResolvedValue({
      revision: 3,
      data: {
        id: 'config-id',
        listId: 'list-id',
        configType: 'bbq',
        configJson: { ...config, excludedGeneratorKeys: ['beef'] },
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z'
      }
    });

    const mutation = await buildGeneratedItemExclusionConfigMutation('list-id', generatedItem('beef'));

    expect(mockedFetchListGenerationConfig).toHaveBeenCalledWith('list-id', 'bbq');
    expect(mutation).not.toBeNull();

    const result = await mutation?.();

    expect(mockedSaveListGenerationConfig).toHaveBeenCalledWith(
      'list-id',
      'bbq',
      expect.objectContaining({
        excludedGeneratorKeys: ['beef']
      })
    );
    expect(result?.revision).toBe(3);
  });

  it('既存の excludedGeneratorKeys を保持して追加する', async () => {
    const config = { ...createDefaultBBQGenerationConfig(), excludedGeneratorKeys: ['pork'] };
    mockedFetchListGenerationConfig.mockResolvedValue({
      id: 'config-id',
      listId: 'list-id',
      configType: 'bbq',
      configJson: config,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z'
    });
    mockedSaveListGenerationConfig.mockResolvedValue({
      revision: 4,
      data: {
        id: 'config-id',
        listId: 'list-id',
        configType: 'bbq',
        configJson: { ...config, excludedGeneratorKeys: ['pork', 'beef'] },
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z'
      }
    });

    const mutation = await buildGeneratedItemExclusionConfigMutation('list-id', generatedItem('beef'));
    await mutation?.();

    expect(mockedSaveListGenerationConfig).toHaveBeenCalledWith(
      'list-id',
      'bbq',
      expect.objectContaining({
        excludedGeneratorKeys: ['pork', 'beef']
      })
    );
  });

  it('generated item でない場合は mutation を作らない', async () => {
    const mutation = await buildGeneratedItemExclusionConfigMutation('list-id', plainItem());

    expect(mutation).toBeNull();
    expect(mockedFetchListGenerationConfig).not.toHaveBeenCalled();
    expect(mockedSaveListGenerationConfig).not.toHaveBeenCalled();
  });

  it('対応するテンプレートがない generatorKey の場合は mutation を作らない', async () => {
    const mutation = await buildGeneratedItemExclusionConfigMutation('list-id', generatedItem('unknown'));

    expect(mutation).toBeNull();
    expect(mockedFetchListGenerationConfig).not.toHaveBeenCalled();
    expect(mockedSaveListGenerationConfig).not.toHaveBeenCalled();
  });
});
