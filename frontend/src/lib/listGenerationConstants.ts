import type { ItemCategory } from '@/types/item-category';
import type { BaseUnit, UnitCode } from '@/types/list-generation';

export const BBQ_GENERATION_RULES = {
  beef: {
    generatorKey: 'beef',
    label: '牛肉',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g' satisfies UnitCode
  },
  pork: {
    generatorKey: 'pork',
    label: '豚肉',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g' satisfies UnitCode
  },
  chicken: {
    generatorKey: 'chicken',
    label: '鶏肉',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g' satisfies UnitCode
  },
  sausage: {
    generatorKey: 'sausage',
    label: 'ソーセージ',
    category: 'meat' satisfies ItemCategory,
    baseUnit: 'g' satisfies BaseUnit,
    displayUnit: 'g' satisfies UnitCode
  },
  vegetableOnion: {
    generatorKey: 'vegetable_onion',
    label: '玉ねぎ',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  },
  vegetableBellPepper: {
    generatorKey: 'vegetable_bell_pepper',
    label: 'ピーマン',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  },
  vegetableCorn: {
    generatorKey: 'vegetable_corn',
    label: 'とうもろこし',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  },
  vegetablePotato: {
    generatorKey: 'vegetable_potato',
    label: 'じゃがいも',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  },
  vegetableMushrooms: {
    generatorKey: 'vegetable_mushrooms',
    label: 'きのこ',
    category: 'vegetables' satisfies ItemCategory,
    baseUnit: 'pack' satisfies BaseUnit,
    displayUnit: 'pack' satisfies UnitCode
  },
  seafoodShrimp: {
    generatorKey: 'seafood_shrimp',
    label: 'えび',
    category: 'seafood' satisfies ItemCategory,
    baseUnit: 'pack' satisfies BaseUnit,
    displayUnit: 'pack' satisfies UnitCode
  },
  seafoodScallop: {
    generatorKey: 'seafood_scallop',
    label: 'ホタテ',
    category: 'seafood' satisfies ItemCategory,
    baseUnit: 'pack' satisfies BaseUnit,
    displayUnit: 'pack' satisfies UnitCode
  },
  seafoodSquid: {
    generatorKey: 'seafood_squid',
    label: 'イカ',
    category: 'seafood' satisfies ItemCategory,
    baseUnit: 'pack' satisfies BaseUnit,
    displayUnit: 'pack' satisfies UnitCode
  },
  softDrinks: {
    generatorKey: 'soft_drinks',
    label: 'ソフトドリンク',
    category: 'drinks' satisfies ItemCategory,
    baseUnit: 'ml' satisfies BaseUnit,
    displayUnit: 'l' satisfies UnitCode
  },
  alcohol: {
    generatorKey: 'alcohol',
    label: 'アルコール(350ml)',
    category: 'drinks' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  },
  yakisoba: {
    generatorKey: 'yakisoba',
    label: '焼きそば',
    category: 'staple' satisfies ItemCategory,
    baseUnit: 'piece' satisfies BaseUnit,
    displayUnit: 'piece' satisfies UnitCode
  }
} as const;

export const BBQ_GENERATOR_KEYS = Object.values(BBQ_GENERATION_RULES).map((rule) => rule.generatorKey);

export const GENERATION_RULES = {
  ...BBQ_GENERATION_RULES
} as const;
