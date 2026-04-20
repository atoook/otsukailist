export const FEEDBACK_LIST_ID = 'f16857f7-f612-4fa3-9786-aca965e13b83';

const baseUrl = import.meta.env.VITE_BASE_URL ?? 'https://otsukailist.atok-reverse-1040.workers.dev';

export const FEEDBACK_URL = `${baseUrl}/lists/${FEEDBACK_LIST_ID}`;
