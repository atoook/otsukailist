<script lang="ts">
import ContentArea from '../components/ContentArea.vue';
import CheckBox from '../components/CheckBox.vue';
import MainButton from '../components/MainButton.vue';
import TextInput from '../components/TextInput.vue';
import ItemBox from '../components/ItemBox.vue';
import DropDown from '../components/DropDown.vue';
import type { Item, ItemId } from '../types/item';
import { ItemStatus } from '../types/item';
import { normalizeText, normalizeInput, normalizeForSearch } from '../utils/text-normalization';
import type { Member, MemberId } from '@/types/member';

export default {
  name: 'ItemListPage',
  components: {
    ContentArea,
    CheckBox,
    MainButton,
    TextInput,
    ItemBox,
    DropDown
  },
  data(): {
    members: Member[];
    listName: string;
    items: Item[];
    newItemName: string;
    searchQuery: string;
    selectedMemberId: MemberId | null;
  } {
    return {
      members: [],
      listName: '',
      items: [],
      newItemName: '',
      searchQuery: '',
      selectedMemberId: null
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

    // TODO: APIからリストデータを取得
    console.log('リストID:', listId);
    console.log('リスト名:', this.listName);
  },
  computed: {
    selectedMember(): Member | undefined {
      return this.members.find((member) => member.id === this.selectedMemberId);
    },
    filteredItems(): Item[] {
      // Ensure items is an array
      const items = Array.isArray(this.items) ? this.items : [];

      const normalizedQuery = normalizeForSearch(this.searchQuery);
      if (!normalizedQuery) {
        return items;
      }
      return items.filter((item) => {
        const normalizedItemName = normalizeForSearch(item.name);
        return normalizedItemName.includes(normalizedQuery);
      });
    }
  },
  methods: {
    initializeMembers() {
      this.members = [
        { id: '1', name: 'しんじ' },
        { id: '2', name: 'Jerry' },
        { id: '3', name: 'けんたろう' },
        { id: '4', name: 'Mike' },
        { id: '5', name: 'トミージャッカーソン' },
        { id: '6', name: 'SomeoneWhoHasLoooooongName' },
        { id: '7', name: 'Ellen' },
        { id: '8', name: 'Daisy' },
        { id: '9', name: 'Lily' },
        { id: '10', name: '太郎' }
      ];

      // Set the default selected member ID
      // default selected to be acquired from LocalStorage
      this.selectedMemberId = '6';
    },
    getMemberBadgeVariant(item: Item): string {
      // 完了済みアイテムで現在選択中のメンバーが割り当てメンバーと同じ場合はprimary（強調）
      if (
        item.status === ItemStatus.COMPLETED &&
        item.assignedMember &&
        this.selectedMember &&
        item.assignedMember.id === this.selectedMember.id
      ) {
        return 'primary';
      }
      // それ以外はsecondary（通常）
      return 'secondary';
    },
    handleMemberSelect(selectedId: string) {
      this.selectedMemberId = selectedId;
    },
    addItem() {
      const normalizedName = normalizeText(this.newItemName);
      if (normalizedName) {
        this.items.push({
          id: Date.now().toString(), //this to be replaced with unique ID from backend
          name: normalizedName,
          status: ItemStatus.PENDING,
          assignedMember: undefined // 初期状態では未割り当て
        });
        this.newItemName = '';
      }
      //sync with backend API here
    },
    // 入力時のリアルタイム正規化
    onItemNameInput(value: string): void {
      this.newItemName = normalizeInput(value);
    },
    onSearchInput(value: string): void {
      this.searchQuery = normalizeInput(value);
    },
    toggleItem(item: Item) {
      const wasCompleted = item.status === ItemStatus.COMPLETED;

      if (wasCompleted) {
        // COMPLETEDからPENDINGに戻す場合：assignedMemberはクリア
        item.status = ItemStatus.PENDING;
        item.assignedMember = undefined;
      } else {
        // PENDINGからCOMPLETEDに変更する場合：現在のselectedMemberを連携
        item.status = ItemStatus.COMPLETED;
        if (this.selectedMember) {
          item.assignedMember = {
            id: this.selectedMember.id,
            name: this.selectedMember.name
          };
        }
      }
    },
    deleteItem(itemId: ItemId) {
      this.items = this.items.filter((item) => item.id !== itemId);
    }
  }
};
</script>

<template>
  <ContentArea>
    <div class="w-full">
      <!-- リストタイトル -->
      <div class="mb-6">
        <h2 class="text-2xl font-bold text-charcoal-800 text-center mb-2">{{ listName }}</h2>
        <p class="text-sm text-charcoal-600 text-center">🍖 買い物リスト</p>
      </div>

      <!-- 新しいアイテム追加 -->
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
          <MainButton @click="addItem" :disabled="!newItemName.trim()"> 追加 </MainButton>
        </div>
      </div>

      <!-- 検索ボックス -->
      <div v-if="items.length > 0" class="mb-4">
        <div class="flex px-2 py-2 border border-charcoal-200 bg-charcoal-100 rounded-md">
          <TextInput
            :type="'search'"
            :model-value="searchQuery"
            @update:model-value="onSearchInput"
            input-name="search"
            placeholder="検索..."
            aria-label="アイテムを検索"
            variant="inline"
          />
        </div>
      </div>
      <!-- チェック時に記録する購入者選択 -->
      <div v-if="filteredItems.length > 0" class="flex justify-end items-center mb-2">
        <div class="flex items-center gap-2 text-sm">
          <span class="text-charcoal-600 font-medium">購入完了者</span>
          <DropDown
            selectName="member"
            :showArrow="true"
            :optionItems="members"
            width="fixed"
            v-model="selectedMemberId"
            @update:modelValue="handleMemberSelect"
          />
        </div>
      </div>
      <!-- アイテムリスト -->
      <div class="space-y-3">
        <ItemBox
          v-for="item in filteredItems"
          :key="item.id"
          :item="item"
          :member="selectedMember"
          :memberBadgeVariant="getMemberBadgeVariant(item)"
          @toggle="toggleItem"
          @delete="deleteItem"
        />

        <!-- アイテムがない場合 -->
        <div v-if="items.length === 0" class="text-center text-charcoal-600 py-8">
          <div class="text-4xl mb-3">🍖</div>
          まだアイテムがありません。<br />
          上のフォームからアイテムを追加してください。
        </div>

        <!-- 検索結果がない場合 -->
        <div v-else-if="filteredItems.length === 0" class="text-center text-charcoal-600 py-8">
          <div class="text-4xl mb-3">🔍</div>
          「{{ searchQuery }}」に一致するアイテムが見つかりませんでした。
        </div>
      </div>
    </div>
  </ContentArea>
</template>
