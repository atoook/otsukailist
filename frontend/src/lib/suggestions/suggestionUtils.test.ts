import { describe, expect, it } from 'vitest';
import { isSuggestionAlreadyAdded, toPlainItemPayload } from '@/lib/suggestions/suggestionUtils';
import type { Item } from '@/types/api';
import type { SuggestionItemDefinition } from '@/lib/suggestions/suggestionTypes';

function plainItem(name: string): Item {
  return {
    id: `item-${name}`,
    name,
    itemType: 'plain',
    category: null,
    preparationType: null,
    quantified: null,
    completed: false,
    assignedMemberId: null,
    completedByMemberId: null,
    completedAt: null,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  };
}

describe('suggestionUtils', () => {
  it('treats normalized exact name matches as already added', () => {
    const suggestion: SuggestionItemDefinition = {
      key: 'bbq_supply_sauce',
      name: 'BBQ ソース',
      category: 'seasonings'
    };

    expect(isSuggestionAlreadyAdded(suggestion, [plainItem('BBQソース')])).toBe(true);
  });

  it('does not treat partial matches as already added', () => {
    const suggestion: SuggestionItemDefinition = {
      key: 'bbq_supply_sauce',
      name: '焼肉のたれ',
      category: 'seasonings'
    };

    expect(isSuggestionAlreadyAdded(suggestion, [plainItem('たれ')])).toBe(false);
  });

  it('converts a suggestion into a plain item payload', () => {
    const suggestion: SuggestionItemDefinition = {
      key: 'bbq_supply_paper_plates',
      name: '紙皿',
      category: 'supplies'
    };

    expect(toPlainItemPayload(suggestion)).toEqual({
      name: '紙皿',
      itemType: 'plain',
      category: 'supplies'
    });
  });
});
