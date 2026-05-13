import { http } from '@/lib/http';
import type { MutationResponse, UUID } from '@/types/api';

export type ListGenerationConfigType = 'bbq';

export type ListGenerationConfigResponse<TConfig = unknown> = {
  id: UUID;
  listId: UUID;
  configType: ListGenerationConfigType;
  configJson: TConfig;
  createdAt: string;
  updatedAt: string;
};

export async function fetchListGenerationConfig<TConfig = unknown>(
  listId: UUID,
  configType: ListGenerationConfigType
) {
  const res = await http.get<ListGenerationConfigResponse<TConfig>>(
    `/lists/${listId}/generation-configs/${configType}`
  );
  return res.data;
}

export async function saveListGenerationConfig<TConfig>(
  listId: UUID,
  configType: ListGenerationConfigType,
  configJson: TConfig
) {
  const res = await http.put<MutationResponse<ListGenerationConfigResponse<TConfig>>>(
    `/lists/${listId}/generation-configs/${configType}`,
    {
      configJson
    }
  );
  return res.data;
}
