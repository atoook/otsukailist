import axios from 'axios';
import type { ApiError } from '@/types/api';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: false
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err?.response?.data;

    if (data?.error && data?.message) {
      return Promise.reject(data as ApiError);
    }

    return Promise.reject({
      error: 'network_error',
      message: err?.message ?? 'Network error',
      timestamp: new Date().toISOString(),
      details: { status: err?.response?.status }
    } satisfies ApiError);
  }
);
