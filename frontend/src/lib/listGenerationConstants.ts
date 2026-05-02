import type { ItemCategory } from '@/types/item-category';
import type { BaseUnit } from '@/types/list-generation';

export const UNIT_DEFINITIONS = {
  g: { code: 'g', baseUnit: 'g', factor: 1 },
  kg: { code: 'kg', baseUnit: 'g', factor: 1000 },
  ml: { code: 'ml', baseUnit: 'ml', factor: 1 },
  l: { code: 'l', baseUnit: 'ml', factor: 1000 },
  piece: { code: 'piece', baseUnit: 'piece', factor: 1 },
  pack: { code: 'pack', baseUnit: 'pack', factor: 1 }
} as const;

export const BBQ_GENERATION_RULES = {
  beef: {
    generatorKey: 'beef',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'kg'
  },
  pork: {
    generatorKey: 'pork',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'kg'
  },
  vegetables: {
    generatorKey: 'vegetables',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g'
  },
  yakisoba: {
    generatorKey: 'yakisoba',
    category: 'staple' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece'
  }
} as const;
