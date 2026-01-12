export interface Member {
  id: MemberId;
  name: string;
}

export type MemberId = string;

export function isMember(obj: any): obj is Member {
  return (
    obj !== null &&
    typeof obj === 'object' &&
    'id' in obj &&
    isMemberId(obj.id) &&
    'name' in obj &&
    typeof obj.name === 'string'
  );
}

// MemberIdの型ガード関数
export function isMemberId(value: any): value is MemberId {
  return typeof value === 'string' || typeof value === 'number';
}
