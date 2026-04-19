import { describe, it, expect, beforeEach } from 'vitest';
import {
  getSelectedMemberId,
  setSelectedMemberId,
  getListHistory,
  addOrUpdateListHistory,
  updateListHistoryName,
  clearListHistory,
  removeListHistoryEntry
} from './userCache';

const LIST_ID_A = 'list-uuid-a';
const LIST_ID_B = 'list-uuid-b';
const MEMBER_ID_1 = 'member-uuid-1';
const MEMBER_ID_2 = 'member-uuid-2';

describe('userCache', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('getSelectedMemberId / setSelectedMemberId', () => {
    it('存在しないlistIdはnullを返す', () => {
      expect(getSelectedMemberId(LIST_ID_A)).toBeNull();
    });

    it('保存したmemberIdを復元できる', () => {
      setSelectedMemberId(LIST_ID_A, MEMBER_ID_1);
      expect(getSelectedMemberId(LIST_ID_A)).toBe(MEMBER_ID_1);
    });

    it('listIdごとに独立して保存される', () => {
      setSelectedMemberId(LIST_ID_A, MEMBER_ID_1);
      setSelectedMemberId(LIST_ID_B, MEMBER_ID_2);
      expect(getSelectedMemberId(LIST_ID_A)).toBe(MEMBER_ID_1);
      expect(getSelectedMemberId(LIST_ID_B)).toBe(MEMBER_ID_2);
    });

    it('上書き保存ができる', () => {
      setSelectedMemberId(LIST_ID_A, MEMBER_ID_1);
      setSelectedMemberId(LIST_ID_A, MEMBER_ID_2);
      expect(getSelectedMemberId(LIST_ID_A)).toBe(MEMBER_ID_2);
    });
  });

  describe('getListHistory', () => {
    it('履歴がない場合は空配列を返す', () => {
      expect(getListHistory()).toEqual([]);
    });

    it('破損したJSONは空配列を返す', () => {
      localStorage.setItem('otsukailist:history', '{invalid json');
      expect(getListHistory()).toEqual([]);
    });

    it('配列でない値は空配列を返す', () => {
      localStorage.setItem('otsukailist:history', JSON.stringify({ listId: LIST_ID_A }));
      expect(getListHistory()).toEqual([]);
    });

    it('不正な構造のエントリを除外して有効なエントリのみ返す', () => {
      localStorage.setItem(
        'otsukailist:history',
        JSON.stringify([
          {
            listId: LIST_ID_A,
            name: 'リストA',
            url: 'https://example.com/lists/a',
            lastAccessedAt: '2026-01-01T00:00:00.000Z'
          },
          { listId: 123, name: 'bad', url: 'x', lastAccessedAt: 'y' }, // listId is not string
          null,
          'string entry'
        ])
      );
      const history = getListHistory();
      expect(history).toHaveLength(1);
      const [first] = history;
      expect(first?.listId).toBe(LIST_ID_A);
    });
  });

  describe('addOrUpdateListHistory', () => {
    it('新しいエントリを追加できる', () => {
      addOrUpdateListHistory({
        listId: LIST_ID_A,
        name: 'リストA'
      });
      const history = getListHistory();
      expect(history).toHaveLength(1);
      const [first] = history;
      expect(first?.listId).toBe(LIST_ID_A);
      expect(first?.name).toBe('リストA');
      expect(typeof first?.lastAccessedAt).toBe('string');
    });

    it('同じlistIdの既存エントリをnameとlastAccessedAtを更新する', () => {
      addOrUpdateListHistory({
        listId: LIST_ID_A,
        name: '旧名前'
      });
      addOrUpdateListHistory({
        listId: LIST_ID_A,
        name: '新名前'
      });
      const history = getListHistory();
      expect(history).toHaveLength(1);
      const [first] = history;
      expect(first?.name).toBe('新名前');
    });

    it('最新アクセスが先頭に来るようにソートされる', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      addOrUpdateListHistory({ listId: LIST_ID_B, name: 'リストB' });
      const history = getListHistory();
      const [first, second] = history;
      expect(first?.listId).toBe(LIST_ID_B);
      expect(second?.listId).toBe(LIST_ID_A);
    });

    it('最大10件に切り詰められる', () => {
      for (let i = 0; i < 15; i++) {
        addOrUpdateListHistory({
          listId: `list-${i}`,
          name: `リスト${i}`
        });
      }
      expect(getListHistory()).toHaveLength(10);
    });

    it('再アクセスでエントリが先頭に移動する', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      addOrUpdateListHistory({ listId: LIST_ID_B, name: 'リストB' });
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      const history = getListHistory();
      const [first, second] = history;
      expect(first?.listId).toBe(LIST_ID_A);
      expect(second?.listId).toBe(LIST_ID_B);
    });
  });

  describe('updateListHistoryName', () => {
    it('存在するエントリのnameを更新できる', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: '旧名前' });
      updateListHistoryName(LIST_ID_A, '新名前');
      const history = getListHistory();
      const [first] = history;
      expect(first?.name).toBe('新名前');
    });

    it('nameを更新してもソート順は変わらない', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      addOrUpdateListHistory({ listId: LIST_ID_B, name: 'リストB' });
      updateListHistoryName(LIST_ID_A, 'リストA改');
      const history = getListHistory();
      const [first, second] = history;
      expect(first?.listId).toBe(LIST_ID_B); // ソート順は変わらない
      expect(second?.name).toBe('リストA改');
    });

    it('存在しないlistIdは何もしない（他のエントリに影響しない）', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      updateListHistoryName('non-existent', '更新');
      const history = getListHistory();
      expect(history).toHaveLength(1);
      const [first] = history;
      expect(first?.name).toBe('リストA');
    });
  });

  describe('clearListHistory', () => {
    it('全履歴を削除できる', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      addOrUpdateListHistory({ listId: LIST_ID_B, name: 'リストB' });
      clearListHistory();
      expect(getListHistory()).toEqual([]);
    });

    it('履歴が空の状態でclearしてもエラーにならない', () => {
      expect(() => clearListHistory()).not.toThrow();
    });
  });

  describe('removeListHistoryEntry', () => {
    it('指定したlistIdのエントリを削除できる', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      addOrUpdateListHistory({ listId: LIST_ID_B, name: 'リストB' });
      removeListHistoryEntry(LIST_ID_A);
      const history = getListHistory();
      expect(history).toHaveLength(1);
      const [first] = history;
      expect(first?.listId).toBe(LIST_ID_B);
    });

    it('存在しないlistIdを指定しても他のエントリに影響しない', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      removeListHistoryEntry('non-existent');
      expect(getListHistory()).toHaveLength(1);
    });

    it('最後の1件を削除すると履歴が空になる', () => {
      addOrUpdateListHistory({ listId: LIST_ID_A, name: 'リストA' });
      removeListHistoryEntry(LIST_ID_A);
      expect(getListHistory()).toEqual([]);
    });
  });
});
