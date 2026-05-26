import type { SuggestionPackDefinition } from '@/lib/suggestions/suggestionTypes';

export const BBQ_SUPPLIES_SUGGESTION_PACK: SuggestionPackDefinition = {
  id: 'bbq_supplies',
  label: 'BBQ 周辺アイテム',
  relatedTemplateId: 'bbq',
  groups: [
    { key: 'tableware', label: '食器' },
    { key: 'cleanup', label: '衛生・片付け' },
    { key: 'cooking', label: '調理器具' },
    { key: 'cooling', label: '保冷' },
    { key: 'seasonings', label: '調味料' },
    { key: 'fire', label: '火おこし・燃料', defaultExpanded: false }
  ],
  items: [
    { key: 'bbq_supply_paper_plates', name: '紙皿', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_paper_cups', name: '紙コップ', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_chopsticks', name: 'はし', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_wet_tissues', name: 'ウェットティッシュ', category: 'daily_goods', group: 'cleanup' },
    { key: 'bbq_supply_kitchen_paper', name: 'キッチンペーパー', category: 'daily_goods', group: 'cleanup' },
    { key: 'bbq_supply_trash_bags', name: 'ゴミ袋', category: 'daily_goods', group: 'cleanup' },
    { key: 'bbq_supply_aluminum_foil', name: 'アルミホイル', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_tongs', name: 'トング', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_kitchen_scissors', name: 'キッチンばさみ', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_knife', name: 'ナイフ', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_cutting_board', name: 'まな板', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_cooler_bag', name: '保冷バッグ', category: 'daily_goods', group: 'cooling' },
    { key: 'bbq_supply_ice_pack', name: '保冷剤', category: 'daily_goods', group: 'cooling' },
    { key: 'bbq_supply_ice', name: '氷', category: 'drinks', group: 'cooling' },
    { key: 'bbq_supply_oil', name: '食用油', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_salt_pepper', name: '塩胡椒', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_sauce', name: '焼肉のたれ', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_butter', name: 'バター', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_chili_pepper', name: '唐辛子', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_charcoal', name: '炭', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_fire_starter', name: '着火剤', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_lighter', name: 'ライター', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_work_gloves', name: '軍手', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_fire_tongs', name: '火ばさみ', category: 'daily_goods', group: 'fire' }
  ]
};
