import { normalizeForSearch } from '@/utils/text-normalization';
import type { CreateItemPayload } from '@/api/item';
import type { Item } from '@/types/api';
import type { SuggestionItemDefinition } from '@/lib/suggestions/suggestionTypes';

export function isSuggestionAlreadyAdded(suggestion: SuggestionItemDefinition, items: Item[]): boolean {
  const normalizedSuggestionName = normalizeForSearch(suggestion.name);
  return items.some((item) => normalizeForSearch(item.name) === normalizedSuggestionName);
}

export function toPlainItemPayload(suggestion: SuggestionItemDefinition): CreateItemPayload {
  return {
    name: suggestion.name,
    itemType: 'plain',
    category: suggestion.category
  };
}
