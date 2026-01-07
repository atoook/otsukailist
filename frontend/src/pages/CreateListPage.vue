<script lang="ts">
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import TextInput from '../components/TextInput.vue';
import BadgeTag from '../components/BadgeTag.vue';
import type { Member, MemberId } from '../types/member';
import type { ItemListId } from '../types/item-list';

export default {
  name: 'CreateListPage',
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
    newMemberName: string;
  } {
    return {
      listName: '',
      members: [],
      newMemberName: ''
    };
  },
  methods: {
    createList(): void {
      if (this.listName.trim()) {
        // リストIDを生成（実際のプロジェクトではAPIから取得）
        const listId: ItemListId = Date.now().toString();

        // TODO: APIでリストを作成
        console.log('リスト名:', this.listName);
        console.log('リストID:', listId);

        // リスト共有画面に遷移
        this.$router.push({
          name: 'ShareList',
          params: { id: listId },
          query: { name: this.listName }
        });
      }
    },
    addMember(): void {
      if (this.newMemberName.trim()) {
        this.members.push({
          id: Date.now(), // this to be replaced with proper unique ID generation from backend
          name: this.newMemberName
        });
        this.newMemberName = '';
      }
    },
    removeMember(memberId: MemberId): void {
      this.members = this.members.filter((member) => member.id !== memberId);
    }
  },
  computed: {
    hasRequiredInput(): boolean {
      return !!this.listName.trim() && this.members.length > 0;
    }
  }
};
</script>

<template>
  <ContentArea>
    <div class="text-center mb-6">
      <div class="text-5xl mb-3">🍖</div>
      <h2 class="text-2xl font-bold font-serif text-charcoal-800 mb-2">リスト名を設定</h2>
      <p class="text-sm text-charcoal-600">美味しい買い物リストを作りましょう</p>
    </div>

    <div class="mb-6">
      <TextInputWithLabel input-id="listName" label="🍖 リスト名" placeholder="例：今日のBBQ材料" v-model="listName" />
    </div>

    <div class="mb-6">
      <label class="block text-sm font-medium text-charcoal-700 mb-2">👥 メンバー</label>
      <div class="flex gap-2 px-2 py-1 border border-wood-200 bg-wood-50 rounded-md">
        <TextInput
          v-model="newMemberName"
          @enter="addMember"
          input-name="newMember"
          placeholder="メンバーを追加..."
          variant="inline"
        />

        <MainButton @click="addMember" :disabled="!newMemberName.trim()" size="small"> 追加 </MainButton>
      </div>

      <!-- メンバーバッジ表示 -->
      <div v-if="members.length > 0" class="mt-3">
        <div class="flex flex-wrap gap-2">
          <BadgeTag
            v-for="member in members"
            :key="member.id"
            :text="member.name"
            icon="👤"
            :removable="true"
            @remove="removeMember(member.id)"
          />
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="createList" :disabled="!hasRequiredInput"> リストを作成 </MainButton>
    </div>
  </ContentArea>
</template>
