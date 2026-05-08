import type { UUID } from '@/types/api';

export const FEEDBACK_LIST_ID: UUID = 'f16857f7-f612-4fa3-9786-aca965e13b83';

const baseUrl = import.meta.env.VITE_BASE_URL ?? window.location.origin;

export const FEEDBACK_URL = `${baseUrl}/lists/${FEEDBACK_LIST_ID}`;
export const CONTACT_FORM_URL = import.meta.env.VITE_CONTACT_FORM_URL;
