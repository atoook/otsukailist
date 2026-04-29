<script lang="ts">
import { defineComponent } from 'vue';
import { NavigationFailureType, isNavigationFailure } from 'vue-router';
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import IconTools from '../components/icons/IconTools.vue';
import IconUsers from '../components/icons/IconUsers.vue';
import type { Item } from '../types/item';
import type { Member, MemberId } from '../types/member';
import { normalizeText } from '../utils/text-normalization';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { updateItem, type UpdateItemPayload } from '@/api/item';
import { fetchSnapshot } from '@/api/list';
import { getErrorMessage } from '@/lib/http';

const UNASSIGNED_MEMBER_VALUE = '';

export default defineComponent({
  name: 'ItemEditPage',
  components: {
    ContentArea,
    MainButton,
    TextInputWithLabel,
    LoadingSpinner,
    IconTools,
    IconUsers
  },
  data(): {
    currentListId: string | null;
    itemName: string;
    members: Member[];
    isCompleted: boolean;
    originalAssignedMemberId: MemberId | null;
    selectedMemberId: MemberId | typeof UNASSIGNED_MEMBER_VALUE;
    currentItemId: string | null;
    errorMessage: string;
    snapshotLoading: boolean;
  } {
    return {
      currentListId: null,
      itemName: '',
      members: [],
      isCompleted: false,
      originalAssignedMemberId: null,
      selectedMemberId: UNASSIGNED_MEMBER_VALUE,
      currentItemId: null,
      errorMessage: '',
      snapshotLoading: false
    };
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  async created() {
    const listId = this.$route.params.id as string | null;
    const itemId = this.$route.params.itemId as string | undefined;
    this.currentListId = listId ?? null;
    this.currentItemId = itemId ?? null;

    if (!listId || !itemId) {
      this.errorMessage = 'アイテムIDが無効です';
      return;
    }

    if (this.listStore.listId !== listId || !this.findCurrentItem()) {
      await this.loadSnapshot(listId);
    }

    if (this.errorMessage) {
      return;
    }

    this.applyCurrentItem();
  },
  methods: {
    findCurrentItem(): Item | undefined {
      return this.listStore.items.find((i: Item) => i.id === this.currentItemId);
    },
    applyCurrentItem(): void {
      const item = this.findCurrentItem();
      if (!item) {
        this.errorMessage = 'アイテムが見つかりませんでした。';
        return;
      }
      this.itemName = item.name;
      this.isCompleted = item.completed;
      this.originalAssignedMemberId = item.assignedMemberId ?? null;
      this.selectedMemberId =
        (item.completed ? item.completedByMemberId : item.assignedMemberId) ?? UNASSIGNED_MEMBER_VALUE;
      this.members = this.listStore.members.map((m) => ({ ...m }));
    },
    async loadSnapshot(listId: string): Promise<void> {
      this.snapshotLoading = true;
      this.errorMessage = '';

      try {
        const snapshot = await fetchSnapshot(listId);
        this.listStore.applySnapshot(snapshot);
      } catch (err: unknown) {
        console.error('Failed to load snapshot', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの取得に失敗しました。';
      } finally {
        this.snapshotLoading = false;
      }
    },
    onItemNameInput(value: string): void {
      this.itemName = value;
    },
    getCurrentSelectedMemberId(): MemberId | null {
      return this.selectedMemberId || null;
    },
    buildUpdatePayload(normalizedName: string): UpdateItemPayload {
      const payload: UpdateItemPayload = {
        name: normalizedName
      };

      if (this.isCompleted) {
        payload.completed = true;
        payload.completedByMemberId = this.getCurrentSelectedMemberId();
        return payload;
      }

      const currentAssignedMemberId = this.getCurrentSelectedMemberId();
      if (currentAssignedMemberId !== this.originalAssignedMemberId) {
        payload.assignedMemberId = currentAssignedMemberId;
      }

      return payload;
    },
    async updateItem(): Promise<void> {
      if (!this.currentListId || !this.currentItemId) {
        this.errorMessage = 'アイテムIDが無効です';
        return;
      }
      const normalizedName = normalizeText(this.itemName);
      if (!normalizedName) {
        this.errorMessage = 'アイテム名を入力してください。';
        return;
      }
      if (this.isCompleted && !this.getCurrentSelectedMemberId()) {
        this.errorMessage = '買った人を選択してください。';
        return;
      }
      try {
        const result = await this.mutationRun(() =>
          updateItem(this.currentListId!, this.currentItemId!, this.buildUpdatePayload(normalizedName))
        );
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.errorMessage = '';
          await this.navigateToItemList('アイテム一覧画面への移動に失敗しました。');
        } else {
          this.errorMessage = 'アイテムの更新が反映されませんでした。時間をおいて再試行してください。';
        }
      } catch (err: unknown) {
        console.error('Failed to update item', err);
        this.errorMessage = getErrorMessage(err) ?? 'アイテムの更新に失敗しました。';
      }
    },
    async cancelUpdate(): Promise<void> {
      if (this.isLoading) {
        return;
      }

      await this.navigateToItemList('リスト画面への移動に失敗しました。');
    },
    async navigateToItemList(fallbackMessage: string): Promise<void> {
      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      try {
        const failure = await this.$router.push({ name: 'ItemList', params: { id: this.currentListId } });
        if (isNavigationFailure(failure, NavigationFailureType.duplicated)) {
          return;
        }
        if (failure) {
          throw failure;
        }
      } catch (err: unknown) {
        if (this.isIgnoredNavigationError(err)) {
          return;
        }
        console.error('Failed to navigate to item list', err);
        this.errorMessage = getErrorMessage(err) ?? fallbackMessage;
      }
    },
    isIgnoredNavigationError(err: unknown): boolean {
      if (isNavigationFailure(err, NavigationFailureType.duplicated)) {
        return true;
      }

      return err instanceof Error && err.name === 'NavigationDuplicated';
    }
  },
  computed: {
    hasRequiredInput(): boolean {
      return !!normalizeText(this.itemName) && (!this.isCompleted || !!this.getCurrentSelectedMemberId());
    },
    isLoading(): boolean {
      return this.mutationLoading || this.snapshotLoading;
    },
    memberLabel(): string {
      return this.isCompleted ? '買った人' : '買う人';
    }
  }
});
</script>

<template>
  <ContentArea v-if="snapshotLoading" layout="center">
    <LoadingSpinner message="リストを読み込み中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="text-center mb-6">
      <div class="text-5xl mb-3 flex justify-center"><IconTools /></div>
      <h2 class="text-2xl font-bold text-charcoal-800">アイテムを編集</h2>
    </div>

    <div class="mb-6">
      <TextInputWithLabel
        input-id="itemName"
        placeholder="例：牛肉"
        :model-value="itemName"
        @update:model-value="onItemNameInput"
      >
        <template #label>アイテム名</template>
      </TextInputWithLabel>
    </div>

    <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
      {{ errorMessage }}
    </div>

    <div class="mb-8">
      <label for="assignedMember" class="flex items-center gap-1 text-sm font-medium text-charcoal-700 mb-2"
        ><IconUsers /> {{ memberLabel }}</label
      >
      <div class="grid grid-cols-2 gap-2 rounded-lg border border-wood-200 bg-wood-50 px-3 py-3">
        <label v-if="!isCompleted" class="min-w-0 flex items-center gap-2 text-sm text-charcoal-700">
          <input
            type="radio"
            name="assignedMember"
            value=""
            v-model="selectedMemberId"
            class="h-4 w-4 accent-wood-500"
          />
          <span class="min-w-0 truncate">未指定</span>
        </label>
        <label
          v-for="member in members"
          :key="member.id"
          class="min-w-0 flex items-center gap-2 text-sm text-charcoal-700"
        >
          <input
            type="radio"
            name="assignedMember"
            :value="member.id"
            v-model="selectedMemberId"
            class="h-4 w-4 accent-wood-500"
          />
          <span class="min-w-0 truncate">{{ member.displayName }}</span>
        </label>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="updateItem" :disabled="!hasRequiredInput || isLoading" variant="primary"> 更新 </MainButton>
      <MainButton @click="cancelUpdate" :disabled="isLoading" variant="secondary"> キャンセル </MainButton>
    </div>
  </ContentArea>
</template>
