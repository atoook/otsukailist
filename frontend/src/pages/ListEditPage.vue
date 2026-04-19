<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import TextInput from '../components/TextInput.vue';
import BadgeTag from '../components/BadgeTag.vue';
import type { Member, MemberId } from '../types/member';
import { normalizeText } from '../utils/text-normalization';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { createMember, deleteMember } from '@/api/member';
import { renameList } from '@/api/list';
import { getErrorMessage } from '@/lib/http';

export default defineComponent({
  name: 'ListEditPage',
  components: {
    ContentArea,
    MainButton,
    TextInputWithLabel,
    TextInput,
    BadgeTag
  },
  data(): {
    listName: string;
    members: Member[];
    selectedMemberId: MemberId | null;
    newMemberName: string;
    currentListId: string | null;
    errorMessage: string;
  } {
    return {
      listName: '',
      members: [],
      selectedMemberId: null,
      newMemberName: '',
      currentListId: null,
      errorMessage: ''
    };
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  created() {
    const listId = this.$route.params.id as string | undefined;
    this.currentListId = listId ?? null;

    if (listId && this.listStore.listId === listId) {
      this.listName = this.listStore.name;
      this.refreshMembersFromStore();
      this.selectedMemberId = this.listStore.members[0]?.id ?? null;
      return;
    }

    const fallbackName = listId ? `リスト${listId}` : this.listName;
    this.listName = fallbackName;
  },
  watch: {
    'listStore.members': {
      handler() {
        if (this.currentListId && this.listStore.listId === this.currentListId) {
          this.refreshMembersFromStore();
        }
      },
      deep: true
    }
  },
  methods: {
    onListNameInput(value: string): void {
      this.listName = value;
    },
    onMemberNameInput(value: string): void {
      this.newMemberName = value;
    },
    async addMember(): Promise<void> {
      const normalizedName = normalizeText(this.newMemberName);
      if (!normalizedName) {
        return;
      }

      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      try {
        const result = await this.mutationRun(() => createMember(this.currentListId!, { displayName: normalizedName }));
        if (result.applied) {
          this.listStore.upsertMember(result.data);
          this.refreshMembersFromStore();
        }
        this.newMemberName = '';
        this.errorMessage = '';
      } catch (err: unknown) {
        console.error('Failed to add member', err);
        this.errorMessage = getErrorMessage(err) ?? 'メンバーの追加に失敗しました。';
      }
    },
    async removeMember(memberId: MemberId): Promise<void> {
      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      try {
        const result = await this.mutationRun(() => deleteMember(this.currentListId!, memberId));
        if (result.applied && result.data.deletedMemberId) {
          this.listStore.removeMember(result.data.deletedMemberId);
          this.refreshMembersFromStore();
          if (this.selectedMemberId === memberId) {
            this.selectedMemberId = this.members[0]?.id ?? null;
          }
        }
        this.errorMessage = '';
      } catch (err: unknown) {
        console.error('Failed to remove member', err);
        this.errorMessage = getErrorMessage(err) ?? 'メンバーの削除に失敗しました。';
      }
    },
    async updateList(): Promise<void> {
      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      const normalizedListName = normalizeText(this.listName);
      if (!normalizedListName || this.members.length === 0) {
        this.errorMessage = 'リスト名とメンバーを確認してください。';
        return;
      }

      this.listName = normalizedListName;

      const shouldRename = this.listStore.listId === this.currentListId && this.listStore.name !== normalizedListName;

      if (shouldRename) {
        try {
          const result = await this.mutationRun(() => renameList(this.currentListId!, { name: normalizedListName }));
          if (!result.applied) {
            this.errorMessage = 'リスト名の更新が反映されませんでした。時間をおいて再試行してください。';
            return;
          }
        } catch (err: unknown) {
          console.error('Failed to rename list', err);
          this.errorMessage = getErrorMessage(err) ?? 'リスト名の更新に失敗しました。';
          return;
        }
      }

      this.listStore.updateListDetails({
        name: this.listName,
        members: [...this.members]
      });
      this.errorMessage = '';
      this.$router.push({
        name: 'ItemList',
        params: { id: this.$route.params.id }
      });
    },
    cancelUpdate(): void {
      if (this.isLoading) {
        return;
      }
      this.$router.back();
    },
    getMemberBadgeVariant(member: Member): string {
      if (this.selectedMemberId === member.id) {
        return 'primary';
      }
      return 'secondary';
    },
    isRemovableMember(member: Member): boolean {
      return this.selectedMemberId !== member.id;
    },
    refreshMembersFromStore() {
      this.members = this.listStore.members.map((member) => ({ ...member }));
    }
  },
  computed: {
    hasRequiredInput(): boolean {
      return !!normalizeText(this.listName) && this.members.length > 0;
    },
    hasValidMemberName(): boolean {
      return !!normalizeText(this.newMemberName);
    },
    isLoading(): boolean {
      return this.mutationLoading;
    }
  }
});
</script>

<template>
  <ContentArea>
    <div class="text-center mb-6">
      <div class="text-5xl mb-3">🛠️</div>
      <h2 class="text-2xl font-bold font-serif text-charcoal-800">リストを編集</h2>
    </div>

    <div class="mb-6">
      <TextInputWithLabel
        input-id="listName"
        label="🍖 リスト名"
        placeholder="例：今日のBBQ材料"
        :model-value="listName"
        @update:model-value="onListNameInput"
      />
    </div>

    <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
      {{ errorMessage }}
    </div>

    <div class="mb-12">
      <label class="block text-sm font-medium text-charcoal-700 mb-2">👥 メンバー</label>
      <div class="flex gap-2 px-2 py-1 border border-wood-200 bg-wood-50 rounded-md">
        <TextInput
          :model-value="newMemberName"
          @update:model-value="onMemberNameInput"
          @enter="addMember"
          input-name="newMember"
          placeholder="メンバーを追加..."
          variant="inline"
        />

        <MainButton @click="addMember" :disabled="!hasValidMemberName || isLoading" size="small"> 追加 </MainButton>
      </div>

      <!-- メンバーバッジ表示 -->
      <div v-if="members.length > 0" class="mt-3">
        <div class="flex flex-wrap gap-2">
          <BadgeTag
            v-for="member in members"
            :key="member.id"
            :text="member.displayName"
            icon="👤"
            :variant="getMemberBadgeVariant(member)"
            :removable="isRemovableMember(member)"
            @remove="removeMember(member.id)"
          />
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="updateList" :disabled="!hasRequiredInput || isLoading" variant="primary"> 更新 </MainButton>
      <MainButton @click="cancelUpdate" :disabled="isLoading" variant="secondary"> キャンセル </MainButton>
    </div>
  </ContentArea>
</template>
