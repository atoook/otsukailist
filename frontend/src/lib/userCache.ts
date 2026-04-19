import type { UUID } from '@/types/api';

const MEMBER_KEY_PREFIX = 'otsukailist:settings:member:';
const HISTORY_KEY = 'otsukailist:history';
const MAX_HISTORY_ENTRIES = 10;

export type ListHistoryEntry = {
  listId: UUID;
  name: string;
  url: string;
  lastAccessedAt: string;
};

// --- selectedMemberId ---

export function getSelectedMemberId(listId: string): string | null {
  try {
    return localStorage.getItem(`${MEMBER_KEY_PREFIX}${listId}`);
  } catch {
    return null;
  }
}

export function setSelectedMemberId(listId: string, memberId: string): void {
  try {
    localStorage.setItem(`${MEMBER_KEY_PREFIX}${listId}`, memberId);
  } catch {
    // ignore (e.g. storage quota exceeded or private-browsing restriction)
  }
}

// --- list history ---

function isValidHistoryEntry(entry: unknown): entry is ListHistoryEntry {
  if (typeof entry !== 'object' || entry === null) return false;
  const e = entry as Record<string, unknown>;
  return (
    typeof e.listId === 'string' &&
    typeof e.name === 'string' &&
    typeof e.url === 'string' &&
    typeof e.lastAccessedAt === 'string'
  );
}

export function getListHistory(): ListHistoryEntry[] {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    if (!raw) return [];
    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(isValidHistoryEntry);
  } catch {
    return [];
  }
}

export function addOrUpdateListHistory(entry: { listId: string; name: string; url: string }): void {
  try {
    const history = getListHistory();
    const now = new Date().toISOString();
    const newEntry: ListHistoryEntry = { ...entry, lastAccessedAt: now };
    // 既存エントリを除去してから先頭に挿入することで、同一ミリ秒の場合も
    // 安定ソート（ES2019+ 保証）により最新アクセスが先頭を保つ
    const filtered = history.filter((h) => h.listId !== entry.listId);
    filtered.unshift(newEntry);
    const sorted = filtered
      .sort((a, b) => b.lastAccessedAt.localeCompare(a.lastAccessedAt))
      .slice(0, MAX_HISTORY_ENTRIES);
    localStorage.setItem(HISTORY_KEY, JSON.stringify(sorted));
  } catch {
    // ignore
  }
}

export function updateListHistoryName(listId: string, name: string): void {
  try {
    const history = getListHistory();
    const idx = history.findIndex((h) => h.listId === listId);
    if (idx < 0) return;
    history[idx] = { ...history[idx]!, name };
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
  } catch {
    // ignore
  }
}

export function clearListHistory(): void {
  try {
    localStorage.removeItem(HISTORY_KEY);
  } catch {
    // ignore
  }
}

export function removeListHistoryEntry(listId: string): void {
  try {
    const history = getListHistory().filter((h) => h.listId !== listId);
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
  } catch {
    // ignore
  }
}
