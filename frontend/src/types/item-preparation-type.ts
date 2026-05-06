export const ITEM_PREPARATION_TYPES = {
  buy: { code: 'buy', label: '購入', sortOrder: 10 },
  bring: { code: 'bring', label: '持参', sortOrder: 20 }
} as const;

export type ItemPreparationType =
  (typeof ITEM_PREPARATION_TYPES)[keyof typeof ITEM_PREPARATION_TYPES]['code'];

export const ITEM_PREPARATION_TYPE_VALUES = Object.values(ITEM_PREPARATION_TYPES).map(
  (preparationType) => preparationType.code
);
