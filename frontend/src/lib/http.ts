import axios from 'axios';
import type { ApiError } from '@/types/api';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isApiError(value: unknown): value is ApiError {
  if (!isRecord(value)) {
    return false;
  }

  const details = value.details;
  return (
    typeof value.error === 'string' &&
    typeof value.message === 'string' &&
    typeof value.timestamp === 'string' &&
    (details === undefined || isRecord(details))
  );
}

function toApiError(data: unknown, fallbackMessage: string, status?: number): ApiError {
  if (isApiError(data)) {
    return data;
  }

  if (isRecord(data) && typeof data.error === 'string' && typeof data.message === 'string') {
    return {
      error: data.error,
      message: data.message,
      timestamp: typeof data.timestamp === 'string' ? data.timestamp : new Date().toISOString(),
      details: isRecord(data.details) ? data.details : { status }
    };
  }

  return {
    error: 'network_error',
    message: fallbackMessage,
    timestamp: new Date().toISOString(),
    details: { status }
  };
}

/**
 * Extracts a user-facing message from an unknown catch value.
 * Handles both ApiError (plain object from interceptor) and Error instances.
 */
export function getErrorMessage(err: unknown): string | null {
  if (
    err !== null &&
    typeof err === 'object' &&
    'message' in err &&
    typeof (err as { message: unknown }).message === 'string'
  ) {
    return (err as { message: string }).message;
  }
  return null;
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: false
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err?.response?.data;
    const apiError = toApiError(data, err?.message ?? 'Network error', err?.response?.status);
    return Promise.reject(apiError);
  }
);
