import type { Member as ApiMember, UUID } from './api';

export type Member = ApiMember;
export type MemberId = UUID;

export function isMember(value: unknown): value is Member {
  return (
    value != null &&
    typeof value === 'object' &&
    'id' in value &&
    isMemberId((value as Member).id) &&
    'displayName' in value &&
    typeof (value as Member).displayName === 'string' &&
    typeof (value as Member).version === 'number'
  );
}

export function isMemberId(value: unknown): value is MemberId {
  return typeof value === 'string';
}
