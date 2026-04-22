<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import TextInput from '../components/TextInput.vue';
import BadgeTag from '../components/BadgeTag.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import IconMeat from '../components/icons/IconMeat.vue';
import IconUsers from '../components/icons/IconUsers.vue';
import IconUser from '../components/icons/IconUser.vue';
import type { Member, MemberId } from '../types/member';
import type { ItemList } from '../types/item-list';
import { normalizeText, normalizeInput } from '../utils/text-normalization';
import { createItemList } from '@/api/list';
import { useListStore } from '@/stores/list';
import { getErrorMessage } from '@/lib/http';

export default defineComponent({
  name: 'CreateListPage',
  components: {
    ContentArea,
    MainButton,
    TextInputWithLabel,
    TextInput,
    BadgeTag,
    LoadingSpinner,
    IconMeat,
    IconUsers,
    IconUser
  },
  data(): {
    listName: string;
    members: Member[];
    newMemberName: string;
    errorMessage: string;
    creating: boolean;
  } {
    return {
      listName: '',
      members: [],
      newMemberName: '',
      errorMessage: '',
      creating: false
    };
  },
  setup() {
    const listStore = useListStore();
    return { listStore };
  },
  methods: {
    async createList(): Promise<void> {
      const normalizedListName = normalizeText(this.listName);
      if (!normalizedListName) {
        return;
      }

      this.errorMessage = '';
      this.listName = normalizedListName;
      this.creating = true;

      try {
        const res = await createItemList({
          name: this.listName,
          memberNames: this.members.map((member) => member.displayName)
        });

        const snapshot: ItemList = {
          listId: res.data.listId,
          name: res.data.name,
          revision: res.revision,
          itemCount: 0,
          lastItemActivityAt: null,
          members: res.data.members,
          items: []
        };

        this.listStore.applySnapshot(snapshot);

        // リスト共有画面に遷移
        this.$router.push({
          name: 'ShareList',
          params: { id: res.data.listId }
        });
      } catch (err: unknown) {
        console.error('Failed to create list', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの作成に失敗しました。';
      } finally {
        this.creating = false;
      }
    },
    addMember(): void {
      const normalizedName = normalizeText(this.newMemberName);
      if (normalizedName) {
        this.members.push({
          id: Date.now().toString(), // this to be replaced with proper unique ID generation from backend
          displayName: normalizedName
        });
        this.newMemberName = '';
      }
    },
    // 入力時のリアルタイム正規化
    onListNameInput(value: string): void {
      this.listName = normalizeInput(value);
    },
    onMemberNameInput(value: string): void {
      this.newMemberName = normalizeInput(value);
    },
    removeMember(memberId: MemberId): void {
      this.members = this.members.filter((member) => member.id !== memberId);
    }
  },
  computed: {
    hasRequiredInput(): boolean {
      return !!normalizeText(this.listName) && this.members.length > 0;
    },
    hasValidMemberName(): boolean {
      return !!normalizeText(this.newMemberName);
    }
  }
});
</script>

<template>
  <ContentArea v-if="creating" layout="center">
    <LoadingSpinner message="リストを作成中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="text-center mb-6">
      <div class="text-5xl mb-3"><IconMeat /></div>
      <h2 class="text-2xl font-bold font-serif text-charcoal-800 mb-2">リスト名を設定</h2>
      <p class="text-sm text-charcoal-600">美味しい買い物リストを作りましょう</p>
    </div>

    <div class="mb-6">
      <TextInputWithLabel
        input-id="listName"
        placeholder="例：今日のBBQ材料"
        :model-value="listName"
        @update:model-value="onListNameInput"
      >
        <template #label><IconMeat /> リスト名</template>
      </TextInputWithLabel>
    </div>

    <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
      {{ errorMessage }}
    </div>

    <div class="mb-6">
      <label class="block text-sm font-medium text-charcoal-700 mb-2"><IconUsers /> メンバー</label>
      <div class="flex gap-2 px-2 py-1 border border-wood-200 bg-wood-50 rounded-md">
        <TextInput
          :model-value="newMemberName"
          @update:model-value="onMemberNameInput"
          @enter="addMember"
          input-name="newMember"
          placeholder="メンバーを追加..."
          variant="inline"
        />

        <MainButton @click="addMember" :disabled="!hasValidMemberName" size="small"> 追加 </MainButton>
      </div>

      <!-- メンバーバッジ表示 -->
      <div v-if="members.length > 0" class="mt-3">
        <div class="flex flex-wrap gap-2">
          <BadgeTag
            v-for="member in members"
            :key="member.id"
            :text="member.displayName"
            :removable="true"
            @remove="removeMember(member.id)"
            ><template #icon><IconUser /></template
          ></BadgeTag>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="createList" :disabled="!hasRequiredInput || creating"> リストを作成 </MainButton>
    </div>
  </ContentArea>
</template>
