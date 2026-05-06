import type { ItemCategory } from '@/types/item-category';
import type { BaseUnit, UnitCode } from '@/types/list-generation';

export const BBQ_GENERATION_RULES = {
  beef: {
    generatorKey: 'beef',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'kg' satisfies UnitCode
  },
  pork: {
    generatorKey: 'pork',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'kg' satisfies UnitCode
  },
  vegetables: {
    generatorKey: 'vegetables',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g' satisfies UnitCode
  },
  yakisoba: {
    generatorKey: 'yakisoba',
    category: 'staple' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  }
} as const;
