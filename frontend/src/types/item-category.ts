export const ITEM_CATEGORY_VALUES = [
  'food',
  'meat',
  'vegetables',
  'seafood',
  'staple',
  'drinks',
  'seasonings',
  'daily_goods',
  'supplies',
  'sweets',
  'other'
] as const;

export type ItemCategory = (typeof ITEM_CATEGORY_VALUES)[number];
