import { describe, it, expect } from 'vitest';
import { collapseSpaces, normalizeText, normalizeForSearch, normalizeInput } from './text-normalization';

describe('text-normalization', () => {
  describe('collapseSpaces', () => {
    it('連続するスペースを単一のスペースに変換', () => {
      expect(collapseSpaces('肉   魚    野菜')).toBe('肉 魚 野菜');
      expect(collapseSpaces('BBQ     材料')).toBe('BBQ 材料');
    });
  });

  describe('normalizeText', () => {
    it('前後の空白を削除し、スペースを正規化', () => {
      expect(normalizeText('  バーベキュー　材料  ')).toBe('バーベキュー 材料');
      expect(normalizeText('BBQ   ソース　　セット   ')).toBe('BBQ ソース セット');
    });

    it('空文字列の場合', () => {
      expect(normalizeText('')).toBe('');
      expect(normalizeText('   ')).toBe('');
    });
  });

  describe('normalizeForSearch', () => {
    it('検索用に正規化（小文字変換・スペース除去）', () => {
      expect(normalizeForSearch('  BBQ　ソース  ')).toBe('bbqソース');
      expect(normalizeForSearch('バーベキュー　肉')).toBe('バーベキュー肉');
    });

    it('様々なスペースパターンを除去', () => {
      expect(normalizeForSearch('BBQ ソース')).toBe('bbqソース');
      expect(normalizeForSearch('BBQ　ソース')).toBe('bbqソース');
      expect(normalizeForSearch('BBQ   ソース')).toBe('bbqソース');
      expect(normalizeForSearch('BBQソース')).toBe('bbqソース');
    });
  });

  describe('normalizeInput', () => {
    it('入力時の軽い正規化（全角スペースのみ変換）', () => {
      expect(normalizeInput('バーベキュー　材料  ')).toBe('バーベキュー 材料  ');
      expect(normalizeInput('BBQ　　ソース')).toBe('BBQ  ソース');
    });

    it('前後空白と連続スペースは保持', () => {
      expect(normalizeInput('  肉　魚  ')).toBe('  肉 魚  ');
      expect(normalizeInput('ソース　　セット   ')).toBe('ソース  セット   ');
    });
  });

  describe('実際のユースケース', () => {
    const testCases = [
      {
        input: 'バーベキュー　肉',
        normalizeInput: 'バーベキュー 肉',
        normalizeText: 'バーベキュー 肉',
        normalizeForSearch: 'バーベキュー肉'
      },
      {
        input: 'BBQ   材料',
        normalizeInput: 'BBQ   材料',
        normalizeText: 'BBQ 材料',
        normalizeForSearch: 'bbq材料'
      },
      {
        input: '  肉　魚  ',
        normalizeInput: '  肉 魚  ',
        normalizeText: '肉 魚',
        normalizeForSearch: '肉魚'
      },
      {
        input: 'ソース　　セット   ',
        normalizeInput: 'ソース  セット   ',
        normalizeText: 'ソース セット',
        normalizeForSearch: 'ソースセット'
      }
    ];

    testCases.forEach(
      ({ input, normalizeInput: expectedInput, normalizeText: expectedText, normalizeForSearch: expectedSearch }) => {
        it(`入力: "${input}" の各段階での正規化`, () => {
          expect(normalizeInput(input)).toBe(expectedInput);
          expect(normalizeText(input)).toBe(expectedText);
          expect(normalizeForSearch(input)).toBe(expectedSearch);
        });
      }
    );
  });
});
