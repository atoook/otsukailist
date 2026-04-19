<script lang="ts">
import { defineComponent } from 'vue';
import MainButton from './MainButton.vue';
import TextInput from './TextInput.vue';
import { normalizeText, normalizeInput } from '../utils/text-normalization';
import { createItem } from '@/api/item';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';

export default defineComponent({
  name: 'ItemAddForm',
  components: { MainButton, TextInput },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  data(): {
    newItemName: string;
  } {
    return {
      newItemName: ''
    };
  },
  emits: ['error'],
  methods: {
    onItemNameInput(value: string): void {
      this.newItemName = normalizeInput(value);
    },
    async addItem() {
      if (this.mutationLoading) {
        return;
      }

      const normalizedName = normalizeText(this.newItemName);
      const listId = this.listStore.listId;

      if (!normalizedName) {
        return;
      }

      if (!listId) {
        this.$emit('error', 'リストが初期化されていません。');
        return;
      }

      try {
        const result = await this.mutationRun(() => createItem(listId, { name: normalizedName }));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.newItemName = '';
        }
      } catch (err: any) {
        console.error('Failed to create item', err);
        this.$emit('error', err?.message ?? 'アイテムの作成に失敗しました。');
      }
    }
  }
});
</script>

<template>
  <div class="mb-6">
    <div class="flex gap-2 px-3 py-3 border border-wood-300 bg-wood-100 rounded-lg shadow-sm">
      <TextInput
        :model-value="newItemName"
        @update:model-value="onItemNameInput"
        @enter="addItem"
        input-name="newItem"
        placeholder="アイテムを追加..."
        variant="inline"
      />
      <MainButton @click="addItem" :disabled="!newItemName.trim() || mutationLoading"> 追加 </MainButton>
    </div>
  </div>
</template>
