/**
 * テキスト正規化ユーティリティ
 * 半角・全角スペースの統一化などを行う
 */

/**
 * 全角スペースを半角スペースに変換（内部ヘルパー関数）
 * @param text 正規化するテキスト
 * @returns 正規化されたテキスト
 */
function normalizeSpaces(text: string): string {
  // 全角スペース（\u3000）を半角スペース（\u0020）に変換
  return text.replace(/\u3000/g, ' ');
}

/**
 * 連続するスペースを単一のスペースに置換
 * @param text 正規化するテキスト
 * @returns 正規化されたテキスト
 */
export function collapseSpaces(text: string): string {
  // 連続する半角スペースを1つにまとめる
  return text.replace(/\s+/g, ' ');
}

/**
 * テキストの前後の空白文字を削除し、スペースを正規化
 * 保存時やデータ処理時に使用
 * @param text 正規化するテキスト
 * @returns 正規化されたテキスト
 */
export function normalizeText(text: string): string {
  return collapseSpaces(normalizeSpaces(text)).trim();
}

/**
 * 検索用にテキストを正規化（小文字変換・スペース除去）
 * @param text 正規化するテキスト
 * @returns 検索用に正規化されたテキスト
 */
export function normalizeForSearch(text: string): string {
  return normalizeText(text).toLowerCase().replace(/\s/g, '');
}

/**
 * 入力時に使用するリアルタイム正規化
 * ユーザビリティを考慮して、入力中は最小限の正規化のみ行う
 * 将来的に入力時特有の処理（文字数制限、特殊文字処理など）を追加予定
 * @param text 入力されたテキスト
 * @returns 軽く正規化されたテキスト
 */
export function normalizeInput(text: string): string {
  // 現在は全角スペースのみ半角に変換
  // 連続スペースの削除やトリムは保存時に行う
  // TODO: 将来的に入力時特有の処理を追加（例：不正文字除去、長さ制限など）
  return normalizeSpaces(text);
}
