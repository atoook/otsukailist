import { describe, expect, it } from 'vitest';
import { formatQuantity, resolveUnitLabel } from './quantityDisplay';

describe('formatQuantity', () => {
  it('g数量はカテゴリに関係なく1000g以上の場合だけkg表示にする', () => {
    expect(formatQuantity({ quantity: 800, baseUnit: 'g', category: 'meat' })).toBe('800g');
    expect(formatQuantity({ quantity: 1100, baseUnit: 'g', category: 'meat' })).toBe('1.1kg');
    expect(formatQuantity({ quantity: 1200, baseUnit: 'g', category: 'seafood' })).toBe('1.2kg');
  });

  it('ml数量は1000ml以上の場合だけL表示にする', () => {
    expect(formatQuantity({ quantity: 500, baseUnit: 'ml', category: 'drinks' })).toBe('500ml');
    expect(formatQuantity({ quantity: 4000, baseUnit: 'ml', category: 'drinks' })).toBe('4L');
    expect(formatQuantity({ quantity: 3150, baseUnit: 'ml', category: 'drinks' })).toBe('3.2L');
  });

  it('packはカテゴリに応じて表示ラベルを変える', () => {
    expect(formatQuantity({ quantity: 2, baseUnit: 'pack', category: 'vegetables' })).toBe('2袋');
    expect(formatQuantity({ quantity: 2, baseUnit: 'pack', category: 'seafood' })).toBe('2パック');
    expect(formatQuantity({ quantity: 2, baseUnit: 'pack', category: 'meat' })).toBe('2パック');
  });

  it('単位ラベルをカテゴリと単位から解決する', () => {
    expect(resolveUnitLabel('pack', 'vegetables')).toBe('袋');
    expect(resolveUnitLabel('pack', 'seafood')).toBe('パック');
    expect(resolveUnitLabel('pack', 'meat')).toBe('パック');
    expect(resolveUnitLabel('piece', 'drinks')).toBe('本');
    expect(resolveUnitLabel('piece', 'seafood')).toBe('個');
  });
});
