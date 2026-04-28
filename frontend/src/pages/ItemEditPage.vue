<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import DropDown from '../components/DropDown.vue';
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
    DropDown,
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
    onSelectMember(memberId: MemberId | typeof UNASSIGNED_MEMBER_VALUE): void {
      this.selectedMemberId = memberId;
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
      try {
        const result = await this.mutationRun(() =>
          updateItem(this.currentListId!, this.currentItemId!, this.buildUpdatePayload(normalizedName))
        );
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.errorMessage = '';
          await this.$router.push({ name: 'ItemList', params: { id: this.currentListId } });
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

      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      try {
        await this.$router.push({ name: 'ItemList', params: { id: this.currentListId } });
      } catch (err: unknown) {
        console.error('Failed to navigate back to item list', err);
        this.errorMessage = getErrorMessage(err) ?? 'リスト画面への移動に失敗しました。';
      }
    }
  },
  computed: {
    hasRequiredInput(): boolean {
      return !!normalizeText(this.itemName);
    },
    isLoading(): boolean {
      return this.mutationLoading || this.snapshotLoading;
    },
    memberLabel(): string {
      return this.isCompleted ? '買った人' : '買う人';
    },
    memberOptions(): Array<{ id: string; name: string }> {
      return [
        { id: UNASSIGNED_MEMBER_VALUE, name: '未指定' },
        ...this.members.map((member) => ({
          id: member.id,
          name: member.displayName
        }))
      ];
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
      <DropDown
        v-if="isCompleted"
        selectId="assignedMember"
        selectName="assignedMember"
        :optionItems="memberOptions"
        :showArrow="true"
        width="full"
        v-model="selectedMemberId"
        @update:modelValue="onSelectMember"
      />
      <div v-else class="grid grid-cols-2 gap-2 rounded-lg border border-wood-200 bg-wood-50 px-3 py-3">
        <label class="min-w-0 flex items-center gap-2 text-sm text-charcoal-700">
          <input
            type="radio"
            name="assignedMember"
            value=""
            :checked="selectedMemberId === ''"
            @change="onSelectMember('')"
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
            :checked="selectedMemberId === member.id"
            @change="onSelectMember(member.id)"
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
