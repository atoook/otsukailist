export const ITEM_PREPARATION_TYPES = {
  bring: { code: 'bring', label: '持参', sortOrder: 10 }
} as const;

export type ItemPreparationType =
  (typeof ITEM_PREPARATION_TYPES)[keyof typeof ITEM_PREPARATION_TYPES]['code'];

export const ITEM_PREPARATION_TYPE_VALUES = Object.values(ITEM_PREPARATION_TYPES).map(
  (preparationType) => preparationType.code
);
