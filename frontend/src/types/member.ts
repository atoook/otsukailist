export interface Member {
  id: MemberId;
  name: string;
}

export type MemberId = string | number;

// MemberIdの型ガード関数
export function isMemberId(value: any): value is MemberId {
  return typeof value === 'string' || typeof value === 'number';
}
