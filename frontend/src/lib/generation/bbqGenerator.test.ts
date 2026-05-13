import { describe, expect, it } from 'vitest';
import {
  createDefaultBBQGenerationConfig,
  generateBBQItems,
  isBBQGenerationConfig,
  type BBQGenerationAnswers,
  type BBQGenerationAdjustments
} from './bbqGenerator';

const answers: BBQGenerationAnswers = {
  adultCount: 6,
  childCount: 2,
  appetite: 'normal',
  balance: 'balanced',
  drinkLevel: 'normal',
  alcoholLevel: 'none',
  alcoholAmount: 'normal',
  seafoodLevel: 'none',
  stapleLevel: 'light'
};

const adjustments: BBQGenerationAdjustments = {
  meat: 'normal',
  seafood: 'normal',
  vegetables: 'normal',
  drinks: 'normal',
  staple: 'normal',
  overall: 'normal'
};

describe('generateBBQItems', () => {
  it('人数から baseUnit 基準の候補を生成する', () => {
    const items = generateBBQItems({ answers, adjustments });

    expect(items).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ generatorKey: 'beef', quantity: 800, baseUnit: 'g', displayUnit: 'g' }),
        expect.objectContaining({ generatorKey: 'pork', quantity: 600, baseUnit: 'g', displayUnit: 'g' }),
        expect.objectContaining({ generatorKey: 'chicken', quantity: 450, baseUnit: 'g', displayUnit: 'g' }),
        expect.objectContaining({ generatorKey: 'sausage', quantity: 300, baseUnit: 'g', displayUnit: 'g' }),
        expect.objectContaining({ generatorKey: 'vegetable_onion', quantity: 2, baseUnit: 'piece' }),
        expect.objectContaining({ generatorKey: 'vegetable_bell_pepper', quantity: 4, baseUnit: 'piece' }),
        expect.objectContaining({ generatorKey: 'vegetable_corn', quantity: 2, baseUnit: 'piece' }),
        expect.objectContaining({ generatorKey: 'vegetable_potato', quantity: 4, baseUnit: 'piece' }),
        expect.objectContaining({ generatorKey: 'vegetable_mushrooms', quantity: 2, baseUnit: 'pack' }),
        expect.objectContaining({ generatorKey: 'soft_drinks', quantity: 4000, baseUnit: 'ml', displayUnit: 'l' }),
        expect.objectContaining({ generatorKey: 'yakisoba', quantity: 5, baseUnit: 'piece' })
      ])
    );
  });

  it('g数量は1000g以上の場合だけ kg 表示にする', () => {
    const items = generateBBQItems({
      answers: { ...answers, adultCount: 10, childCount: 0 },
      adjustments
    });

    expect(items).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ generatorKey: 'beef', quantity: 1100, baseUnit: 'g', displayUnit: 'kg' }),
        expect.objectContaining({ generatorKey: 'pork', quantity: 800, baseUnit: 'g', displayUnit: 'g' })
      ])
    );
  });

  it('adjustments を数量に反映する', () => {
    const normalItems = generateBBQItems({ answers, adjustments });
    const largeMeatItems = generateBBQItems({
      answers,
      adjustments: { ...adjustments, meat: 'large' }
    });

    const normalBeef = normalItems.find((item) => item.generatorKey === 'beef');
    const largeBeef = largeMeatItems.find((item) => item.generatorKey === 'beef');
    const normalVegetables = normalItems.find((item) => item.generatorKey === 'vegetable_bell_pepper');
    const largeVegetables = largeMeatItems.find((item) => item.generatorKey === 'vegetable_bell_pepper');

    expect(largeBeef?.quantity).toBeGreaterThan(normalBeef?.quantity ?? 0);
    expect(largeVegetables?.quantity).toBe(normalVegetables?.quantity);
  });

  it('主食なしの場合は焼きそば候補を出さない', () => {
    const items = generateBBQItems({
      answers: { ...answers, stapleLevel: 'none' },
      adjustments
    });

    expect(items.some((item) => item.generatorKey === 'yakisoba')).toBe(false);
  });

  it('アルコールありの場合だけアルコール候補を出す', () => {
    const withoutAlcohol = generateBBQItems({ answers, adjustments });
    const withAlcohol = generateBBQItems({
      answers: { ...answers, alcoholLevel: 'included' },
      adjustments
    });

    expect(withoutAlcohol.some((item) => item.generatorKey === 'alcohol')).toBe(false);
    expect(withAlcohol).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          generatorKey: 'alcohol',
          name: 'アルコール(350ml)',
          quantity: 9,
          baseUnit: 'piece',
          displayUnit: 'piece'
        })
      ])
    );
  });

  it('アルコール普通は大人1人あたり500ml相当を350ml缶の本数で生成する', () => {
    const items = generateBBQItems({
      answers: { ...answers, adultCount: 4, childCount: 0, alcoholLevel: 'included', alcoholAmount: 'normal' },
      adjustments
    });

    expect(items).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ generatorKey: 'alcohol', quantity: 6, baseUnit: 'piece', displayUnit: 'piece' })
      ])
    );
  });

  it('アルコール量の回答をアルコール候補に反映する', () => {
    const normalAlcoholItems = generateBBQItems({
      answers: { ...answers, alcoholLevel: 'included', alcoholAmount: 'normal' },
      adjustments
    });
    const largeAlcoholItems = generateBBQItems({
      answers: { ...answers, alcoholLevel: 'included', alcoholAmount: 'large' },
      adjustments
    });
    const normalAlcohol = normalAlcoholItems.find((item) => item.generatorKey === 'alcohol');
    const largeAlcohol = largeAlcoholItems.find((item) => item.generatorKey === 'alcohol');

    expect(largeAlcohol?.quantity).toBeGreaterThan(normalAlcohol?.quantity ?? 0);
  });

  it('飲み物の調整をソフトドリンク数量に反映する', () => {
    const normalItems = generateBBQItems({ answers, adjustments });
    const largeDrinkItems = generateBBQItems({
      answers,
      adjustments: { ...adjustments, drinks: 'large' }
    });

    const normalDrinks = normalItems.find((item) => item.generatorKey === 'soft_drinks');
    const largeDrinks = largeDrinkItems.find((item) => item.generatorKey === 'soft_drinks');

    expect(largeDrinks?.quantity).toBeGreaterThan(normalDrinks?.quantity ?? 0);
  });

  it('海鮮の調整を海鮮数量に反映する', () => {
    const normalItems = generateBBQItems({
      answers: { ...answers, seafoodLevel: 'included' },
      adjustments
    });
    const largeSeafoodItems = generateBBQItems({
      answers: { ...answers, seafoodLevel: 'included' },
      adjustments: { ...adjustments, seafood: 'large' }
    });

    const normalShrimp = normalItems.find((item) => item.generatorKey === 'seafood_shrimp');
    const largeShrimp = largeSeafoodItems.find((item) => item.generatorKey === 'seafood_shrimp');

    expect(largeShrimp?.quantity).toBeGreaterThan(normalShrimp?.quantity ?? 0);
  });

  it('主食の調整を焼きそば数量に反映する', () => {
    const normalItems = generateBBQItems({ answers, adjustments });
    const largeStapleItems = generateBBQItems({
      answers,
      adjustments: { ...adjustments, staple: 'large' }
    });

    const normalYakisoba = normalItems.find((item) => item.generatorKey === 'yakisoba');
    const largeYakisoba = largeStapleItems.find((item) => item.generatorKey === 'yakisoba');

    expect(largeYakisoba?.quantity).toBeGreaterThan(normalYakisoba?.quantity ?? 0);
  });

  it('海鮮ありの場合は海鮮候補を出し肉量を少し抑える', () => {
    const withoutSeafood = generateBBQItems({ answers, adjustments });
    const withSeafood = generateBBQItems({
      answers: { ...answers, seafoodLevel: 'included' },
      adjustments
    });

    const normalBeef = withoutSeafood.find((item) => item.generatorKey === 'beef');
    const seafoodBeef = withSeafood.find((item) => item.generatorKey === 'beef');

    expect(withSeafood).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ generatorKey: 'seafood_shrimp', quantity: 2, baseUnit: 'pack' }),
        expect.objectContaining({ generatorKey: 'seafood_scallop', quantity: 2, baseUnit: 'pack' }),
        expect.objectContaining({ generatorKey: 'seafood_squid', quantity: 2, baseUnit: 'pack' })
      ])
    );
    expect(seafoodBeef?.quantity).toBeLessThan(normalBeef?.quantity ?? 0);
  });
});

describe('isBBQGenerationConfig', () => {
  it('デフォルト config を有効な config として判定する', () => {
    expect(isBBQGenerationConfig(createDefaultBBQGenerationConfig())).toBe(true);
  });
});
