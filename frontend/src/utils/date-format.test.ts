import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { formatActivityAt } from './date-format';

describe('formatActivityAt', () => {
  it('null を渡すと null を返す', () => {
    expect(formatActivityAt(null)).toBeNull();
  });

  it('undefined を渡すと null を返す', () => {
    expect(formatActivityAt(undefined)).toBeNull();
  });

  it('空文字列を渡すと null を返す', () => {
    expect(formatActivityAt('')).toBeNull();
  });

  it('不正な日付文字列を渡すと null を返す', () => {
    expect(formatActivityAt('not-a-date')).toBeNull();
    expect(formatActivityAt('2024-13-99T00:00:00Z')).toBeNull();
  });

  describe('当日のとき', () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2024-06-15T12:00:00Z'));
    });
    afterEach(() => {
      vi.useRealTimers();
    });

    it('HH:mm 形式でフォーマットされる', () => {
      const result = formatActivityAt('2024-06-15T10:30:00Z');
      expect(result).not.toBeNull();
      // 時刻部分が含まれていること（ロケール依存だが ":" が含まれる）
      expect(result).toMatch(/\d{1,2}:\d{2}/);
    });
  });

  describe('別日のとき', () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2024-06-15T12:00:00Z'));
    });
    afterEach(() => {
      vi.useRealTimers();
    });

    it('月・日・時刻を含む形式でフォーマットされる', () => {
      const result = formatActivityAt('2024-06-10T08:00:00Z');
      expect(result).not.toBeNull();
      expect(result).toMatch(/\d{1,2}/); // 月または日が含まれる
      expect(result).toMatch(/\d{1,2}:\d{2}/); // 時刻が含まれる
    });
  });
});
