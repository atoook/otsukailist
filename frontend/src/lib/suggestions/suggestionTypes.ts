import type { ItemCategory } from '@/types/item-category';

export type SuggestionGroupDefinition = {
  key: string;
  label: string;
  defaultExpanded?: boolean;
};

export type SuggestionGroupKey = SuggestionGroupDefinition['key'];

export type SuggestionItemDefinition = {
  key: string;
  name: string;
  category: ItemCategory;
  group?: SuggestionGroupKey;
};

export type SuggestionPackDefinition = {
  id: string;
  label: string;
  relatedTemplateId?: string;
  groups?: SuggestionGroupDefinition[];
  items: SuggestionItemDefinition[];
};
