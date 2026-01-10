// アイテムの型定義
export interface Item {
  id: ItemId;
  name: string;
  status: ItemStatus;
}

// アイテムのステータスをenumで定義
export enum ItemStatus {
  PENDING = 'pending',
  COMPLETED = 'completed',
  ARCHIVED = 'archived'
}

// 型ガード関数（ランタイムでの型チェック）
export function isItem(obj: any): obj is Item {
  return (
    obj !== null &&
    typeof obj === 'object' &&
    'id' in obj &&
    isItemId(obj.id) &&
    'name' in obj &&
    typeof obj.name === 'string' &&
    'status' in obj &&
    Object.values(ItemStatus).includes(obj.status)
  );
}

// その他のアイテム関連の型定義
export type ItemId = string | number;

// ItemIdの型ガード関数
export function isItemId(value: any): value is ItemId {
  return typeof value === 'string' || typeof value === 'number';
}

// ステータス関連のヘルパー関数
export function isCompletedStatus(status: ItemStatus): boolean {
  return status === ItemStatus.COMPLETED;
}

export function isPendingStatus(status: ItemStatus): boolean {
  return status === ItemStatus.PENDING;
}

export function isArchivedStatus(status: ItemStatus): boolean {
  return status === ItemStatus.ARCHIVED;
}
