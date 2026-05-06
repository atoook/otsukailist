export type ItemType = 'plain' | 'quantified';

export type BaseUnit = 'g' | 'ml' | 'piece' | 'pack';

export type ItemOrigin = 'manual' | 'generated';

export type RegenerationPolicy = 'none' | 'auto' | 'locked';

export type QuantifiedItem = {
  quantity: number;
  baseUnit: BaseUnit;
  origin: ItemOrigin;
  regenerationPolicy: RegenerationPolicy;
  generatorKey: string | null;
};
