export const CONTACT_FORM_URL = import.meta.env.VITE_CONTACT_FORM_URL;

const DEFAULT_MAX_MEMBERS_PER_LIST = 20;
const parsedMaxMembersPerList = Number.parseInt(import.meta.env.VITE_MAX_MEMBERS_PER_LIST ?? '', 10);

export const MAX_MEMBERS_PER_LIST =
  Number.isFinite(parsedMaxMembersPerList) && parsedMaxMembersPerList > 0
    ? parsedMaxMembersPerList
    : DEFAULT_MAX_MEMBERS_PER_LIST;
