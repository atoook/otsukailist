export const ITEM_CATEGORIES = {
  food: { code: 'food', label: '食品', sortOrder: 10 },
  meat: { code: 'meat', label: '肉', sortOrder: 20 },
  vegetables: { code: 'vegetables', label: '野菜', sortOrder: 30 },
  seafood: { code: 'seafood', label: '海鮮', sortOrder: 40 },
  staple: { code: 'staple', label: '主食', sortOrder: 50 },
  drinks: { code: 'drinks', label: '飲み物', sortOrder: 60 },
  seasonings: { code: 'seasonings', label: '調味料', sortOrder: 70 },
  sweets: { code: 'sweets', label: 'お菓子', sortOrder: 80 },
  dailyGoods: { code: 'daily_goods', label: '日用品', sortOrder: 90 },
  supplies: { code: 'supplies', label: '消耗品', sortOrder: 100 },
  other: { code: 'other', label: 'その他', sortOrder: 999 }
} as const;

export type ItemCategory = (typeof ITEM_CATEGORIES)[keyof typeof ITEM_CATEGORIES]['code'];

export const ITEM_CATEGORY_VALUES = Object.values(ITEM_CATEGORIES).map((category) => category.code);
