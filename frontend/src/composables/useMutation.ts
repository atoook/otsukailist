import { computed, ref } from 'vue';
import type { ApiError, MutationResponse } from '@/types/api';
import { fetchSnapshot } from '@/api/list';
import { useListStore } from '@/stores/list';

export type MutationRunResult<T> = {
  data: T;
  applied: boolean;
  revision: number;
};

export function useMutation() {
  const listStore = useListStore();

  const loading = ref(false);
  const error = ref<ApiError | null>(null);
  const hasError = computed(() => error.value != null);

  async function run<T>(fn: () => Promise<MutationResponse<T>>): Promise<MutationRunResult<T>> {
    loading.value = true;
    error.value = null;

    try {
      const res = await fn();
      const currentRevision = listStore.revision;
      const nextRevision = res.revision;
      const gap = nextRevision - currentRevision;

      if (nextRevision <= currentRevision) {
        return { data: res.data, applied: false, revision: nextRevision };
      }

      listStore.setRevision(nextRevision);

      if (gap > 1 && listStore.listId) {
        const snapshot = await fetchSnapshot(listStore.listId);
        listStore.applySnapshot(snapshot);
        return { data: res.data, applied: false, revision: nextRevision };
      }

      return { data: res.data, applied: true, revision: nextRevision };
    } catch (err) {
      error.value = err as ApiError;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    run,
    loading,
    error,
    hasError
  };
}
