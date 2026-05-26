export const ITEM_TYPE_VALUES = ['plain', 'quantified'] as const;

export type ItemType = (typeof ITEM_TYPE_VALUES)[number];

export const UNIT_DEFINITIONS = {
  g: { code: 'g', baseUnit: 'g', factor: 1 },
  kg: { code: 'kg', baseUnit: 'g', factor: 1000 },
  ml: { code: 'ml', baseUnit: 'ml', factor: 1 },
  l: { code: 'l', baseUnit: 'ml', factor: 1000 },
  piece: { code: 'piece', baseUnit: 'piece', factor: 1 },
  pack: { code: 'pack', baseUnit: 'pack', factor: 1 }
} as const;

export type UnitCode = keyof typeof UNIT_DEFINITIONS;

export type BaseUnit = (typeof UNIT_DEFINITIONS)[UnitCode]['baseUnit'];

export const BASE_UNIT_VALUES = Object.values(UNIT_DEFINITIONS)
  .filter((definition) => definition.code === definition.baseUnit)
  .map((definition) => definition.baseUnit);

export const ITEM_ORIGIN_VALUES = ['manual', 'generated'] as const;

export type ItemOrigin = (typeof ITEM_ORIGIN_VALUES)[number];

export const REGENERATION_POLICY_VALUES = ['none', 'auto', 'locked'] as const;

export type RegenerationPolicy = (typeof REGENERATION_POLICY_VALUES)[number];

export type QuantifiedItem = {
  quantity: number;
  baseUnit: BaseUnit;
  origin: ItemOrigin;
  regenerationPolicy: RegenerationPolicy;
  generatorKey: string | null;
};
