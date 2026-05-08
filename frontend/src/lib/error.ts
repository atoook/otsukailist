import type { ApiError } from '@/types/api';

export function getFieldErrors(err: ApiError): Record<string, string> {
  const fieldErrors =
    err?.details && typeof err.details === 'object' ? (err.details as Record<string, unknown>).fieldErrors : undefined;

  if (fieldErrors && typeof fieldErrors === 'object') {
    return Object.fromEntries(
      Object.entries(fieldErrors).filter((entry): entry is [string, string] => typeof entry[1] === 'string')
    );
  }

  return {};
}
