<script lang="ts">
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInputWithLabel from '../components/TextInputWithLabel.vue';
import TextInput from '../components/TextInput.vue';
import BadgeTag from '../components/BadgeTag.vue';
import type { Member, MemberId } from '../types/member';
import { normalizeText, normalizeInput } from '../utils/text-normalization';

export default {
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
  } {
    return {
      listName: '',
      members: [],
      selectedMemberId: null,
      newMemberName: ''
    };
  },
  created() {
    // ルートパラメータからリストIDを取得
    const listId = this.$route.params.id;
    // クエリパラメータからリスト名を取得(TODO: APIから取得する)
    const listName = this.$route.query.name as string | undefined;
    this.listName = listName || `リスト${listId}`;

    // メンバーデータの初期化
    this.initializeMembers();
  },
  methods: {
    initializeMembers() {
      this.members = [
        { id: '1', name: 'しんじ' },
        { id: '2', name: 'Jerry' },
        { id: '3', name: 'けんたろう' },
        { id: '4', name: 'Mike' },
        { id: '5', name: 'トミージャッカーソン' },
        { id: '6', name: 'ハリーポッターストレンジャーシングス' },
        { id: '7', name: 'Ellen' },
        { id: '8', name: 'Daisy' },
        { id: '9', name: 'Lily' },
        { id: '10', name: '太郎' }
      ];
      // Set the default selected member ID
      // default selected to be acquired from LocalStorage
      this.selectedMemberId = '6';
    },
    onListNameInput(value: string): void {
      this.listName = value;
    },
    onMemberNameInput(value: string): void {
      this.newMemberName = value;
    },
    addMember(): void {
      const trimmedName = this.newMemberName.trim();
      if (trimmedName) {
        this.members.push({
          id: Date.now().toString(), // this to be replaced with proper unique ID generation from backend
          name: trimmedName
        });
        this.newMemberName = '';
      }
    },
    removeMember(memberId: MemberId): void {
      // TODO : メンバーがアイテムに割り当てられている場合の処理
      // TODO : APIでメンバー削除
      this.members = this.members.filter((member) => member.id !== memberId);
    },
    updateList(): void {
      const normalizedListName = normalizeText(this.listName);
      if (normalizedListName && this.members.length > 0) {
        // 正規化されたリスト名で保存
        this.listName = normalizedListName;
      }
      // TODO: APIでリストを更新して保存
      // リスト詳細画面に遷移
      this.$router.push({
        name: 'ItemList',
        params: { id: this.$route.params.id },
        query: { name: this.listName }
      });
    },
    cancelUpdate(): void {
      this.$router.back();
    },
    getMemberBadgeVariant(member: Member): string {
      // 現在選択中のメンバーはprimary（強調）
      if (this.selectedMemberId === member.id) {
        return 'primary';
      }
      // それ以外はsecondary（通常）
      return 'secondary';
    },
    isRemovableMember(member: Member): boolean {
      // 選択中のメンバーは削除不可にしているが、将来的にはアイテム割り当てチェックも追加予定
      return this.selectedMemberId !== member.id;
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
};
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

        <MainButton @click="addMember" :disabled="!hasValidMemberName" size="small"> 追加 </MainButton>
      </div>

      <!-- メンバーバッジ表示 -->
      <div v-if="members.length > 0" class="mt-3">
        <div class="flex flex-wrap gap-2">
          <BadgeTag
            v-for="member in members"
            :key="member.id"
            :text="member.name"
            icon="👤"
            :variant="getMemberBadgeVariant(member)"
            :removable="isRemovableMember(member)"
            @remove="removeMember(member.id)"
          />
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="updateList" :disabled="!hasRequiredInput" variant="primary"> 更新 </MainButton>
      <MainButton @click="cancelUpdate" variant="secondary"> キャンセル </MainButton>
    </div>
  </ContentArea>
</template>
