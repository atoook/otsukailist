export const ITEM_TYPE_VALUES = ['plain', 'quantified'] as const;

export type ItemType = (typeof ITEM_TYPE_VALUES)[number];

export const BASE_UNIT_VALUES = ['g', 'ml', 'piece', 'pack'] as const;

export type BaseUnit = (typeof BASE_UNIT_VALUES)[number];

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
