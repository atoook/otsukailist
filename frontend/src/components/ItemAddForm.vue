<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import MainButton from './MainButton.vue';
import TextInput from './TextInput.vue';
import { normalizeText, normalizeInput } from '../utils/text-normalization';
import { createItem } from '@/api/item';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { getErrorMessage } from '@/lib/http';
import { ITEM_CATEGORIES } from '@/types/item-category';
import type { ItemCategory } from '@/types/item-category';
import { ITEM_PREPARATION_TYPES } from '@/types/item-preparation-type';
import type { ItemPreparationType } from '@/types/item-preparation-type';
import type { MemberId } from '@/types/member';

export default defineComponent({
  name: 'ItemAddForm',
  components: { MainButton, TextInput },
  props: {
    categoryFilter: {
      type: String as PropType<ItemCategory | null>,
      default: null
    },
    preparationTypeFilter: {
      type: String as PropType<ItemPreparationType | null>,
      default: null
    },
    memberFilterId: {
      type: String as PropType<MemberId | null>,
      default: null
    }
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  data(): {
    newItemName: string;
    inputFocused: boolean;
  } {
    return {
      newItemName: '',
      inputFocused: false
    };
  },
  computed: {
    inheritedFilterLabels(): string[] {
      return [
        this.categoryFilter ? ITEM_CATEGORIES[this.categoryFilter]?.label : null,
        this.preparationTypeFilter ? ITEM_PREPARATION_TYPES[this.preparationTypeFilter]?.label : null,
        this.memberFilterId ? this.listStore.memberMap.get(this.memberFilterId)?.displayName : null
      ].filter((label): label is string => Boolean(label));
    },
    shouldShowInheritedFilterHint(): boolean {
      return this.inheritedFilterLabels.length > 0 && (this.inputFocused || this.newItemName.trim().length > 0);
    }
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
        const result = await this.mutationRun(() =>
          createItem(listId, {
            name: normalizedName,
            category: this.categoryFilter,
            preparationType: this.preparationTypeFilter,
            assignedMemberId: this.memberFilterId
          })
        );
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.newItemName = '';
        }
      } catch (err: unknown) {
        console.error('Failed to create item', err);
        this.$emit('error', getErrorMessage(err) ?? 'アイテムの作成に失敗しました。');
      }
    }
  }
});
</script>

<template>
  <div class="mb-6">
    <div
      class="flex gap-2 px-3 py-3 border border-wood-300 bg-wood-100 rounded-lg shadow-sm"
      @focusin="inputFocused = true"
      @focusout="inputFocused = false"
    >
      <TextInput
        :model-value="newItemName"
        @update:model-value="onItemNameInput"
        @enter="addItem"
        input-name="newItem"
        placeholder="アイテムを追加..."
        variant="inline"
      />
      <MainButton @click="addItem" :disabled="!newItemName.trim() || mutationLoading">
        {{ mutationLoading ? '追加中' : '追加' }}
      </MainButton>
    </div>
    <p v-if="shouldShowInheritedFilterHint" class="mt-1 px-1 text-xs text-charcoal-600">
      {{ inheritedFilterLabels.join(' / ') }}で追加されます
    </p>
  </div>
</template>
