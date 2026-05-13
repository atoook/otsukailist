/**
 * 日付フォーマットユーティリティ（ja-JP ロケール向け）
 */

/**
 * ISO 8601 文字列を日本語表示用にフォーマットする。
 * - 当日: "HH:mm"
 * - 別日: "M月D日 HH:mm"
 * - 無効な文字列 / null の場合: null を返す
 */
export function formatActivityAt(isoString: string | null | undefined): string | null {
  if (!isoString) return null;
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return null;

  const now = new Date();
  const isToday =
    date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth() && date.getDate() === now.getDate();

  if (isToday) {
    return new Intl.DateTimeFormat('ja-JP', { hour: '2-digit', minute: '2-digit' }).format(date);
  }
  return new Intl.DateTimeFormat('ja-JP', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}
