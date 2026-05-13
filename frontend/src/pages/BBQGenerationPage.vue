<script lang="ts">
import { defineComponent } from 'vue';
import BBQGenerationPanel from '../components/BBQGenerationPanel.vue';
import ContentArea from '../components/ContentArea.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import MainButton from '../components/MainButton.vue';
import { fetchSnapshot } from '@/api/list';
import { fetchListGenerationConfig } from '@/api/listGenerationConfig';
import { isBBQGenerationConfig, type BBQGenerationConfig } from '@/lib/generation/bbqGenerator';
import { getErrorMessage } from '@/lib/http';
import { addOrUpdateListHistory } from '@/lib/userCache';
import { useListStore } from '@/stores/list';
import type { ApiError } from '@/types/api';

export default defineComponent({
  name: 'BBQGenerationPage',
  components: {
    ContentArea,
    BBQGenerationPanel,
    LoadingSpinner,
    MainButton
  },
  setup() {
    const listStore = useListStore();
    return { listStore };
  },
  data(): {
    currentListId: string | null;
    generationConfig: BBQGenerationConfig | null;
    loading: boolean;
    errorMessage: string;
  } {
    return {
      currentListId: null,
      generationConfig: null,
      loading: false,
      errorMessage: ''
    };
  },
  async created() {
    const listId = this.$route.params.id as string | undefined;
    this.currentListId = listId ?? null;

    if (!listId) {
      this.errorMessage = 'リストIDが指定されていません。';
      return;
    }

    await this.loadPageData(listId);
  },
  computed: {
    listName(): string {
      return this.listStore.name || '買い物リスト';
    },
    hasExistingConfig(): boolean {
      return this.generationConfig !== null;
    }
  },
  methods: {
    async loadPageData(listId: string): Promise<void> {
      this.loading = true;
      this.errorMessage = '';

      try {
        const snapshot = await fetchSnapshot(listId);
        this.listStore.applySnapshot(snapshot);
        addOrUpdateListHistory({ listId, name: snapshot.name });
        await this.loadGenerationConfig(listId);
      } catch (err: unknown) {
        console.error('Failed to load BBQ generation page', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの取得に失敗しました。';
      } finally {
        this.loading = false;
      }
    },
    async loadGenerationConfig(listId: string): Promise<void> {
      try {
        const res = await fetchListGenerationConfig<unknown>(listId, 'bbq');
        this.generationConfig = isBBQGenerationConfig(res.configJson) ? res.configJson : null;
      } catch (err: unknown) {
        if (this.isNotFoundError(err)) {
          this.generationConfig = null;
          return;
        }
        throw err;
      }
    },
    isNotFoundError(err: unknown): boolean {
      return err !== null && typeof err === 'object' && 'error' in err && (err as ApiError).error === 'not_found';
    },
    navigateToList(): void {
      if (!this.currentListId) {
        this.$router.push({ name: 'Welcome' });
        return;
      }
      this.$router.push({
        name: 'ItemList',
        params: { id: this.currentListId }
      });
    },
    handleGenerationCompleted(config: BBQGenerationConfig): void {
      this.generationConfig = config;
      this.navigateToList();
    }
  }
});
</script>

<template>
  <ContentArea v-if="loading" layout="center">
    <LoadingSpinner message="BBQ テンプレートを読み込み中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="mx-auto w-full max-w-3xl">
      <div class="mb-5">
        <button type="button" class="mb-3 text-sm font-medium text-charcoal-500 hover:text-charcoal-800" @click="navigateToList">
          ← リストに戻る
        </button>
        <h2 class="text-2xl font-black text-charcoal-800">BBQ テンプレート</h2>
        <p class="mt-1 text-sm text-charcoal-600">{{ listName }} に追加する買い物候補を作ります</p>
      </div>

      <div v-if="errorMessage" class="mb-4 rounded-lg border border-ember-300 bg-ember-100 p-3 text-sm text-ember-700">
        {{ errorMessage }}
      </div>

      <BBQGenerationPanel
        v-if="!errorMessage"
        :initial-config="generationConfig"
        :has-existing-config="hasExistingConfig"
        :show-header="false"
        @completed="handleGenerationCompleted"
        @cancel="navigateToList"
        @error="errorMessage = $event"
      />

      <div v-else class="mt-5">
        <MainButton variant="secondary" @click="navigateToList">リストに戻る</MainButton>
      </div>
    </div>
  </ContentArea>
</template>
