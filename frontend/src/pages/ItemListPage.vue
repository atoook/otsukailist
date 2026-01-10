<script lang="ts">
import ContentArea from '../components/ContentArea.vue';
import CheckBox from '../components/CheckBox.vue';
import MainButton from '../components/MainButton.vue';
import TextInput from '../components/TextInput.vue';
import ItemBox from '../components/ItemBox.vue';
import type { Item, ItemId } from '../types/item';
import { ItemStatus } from '../types/item';
import { normalizeText, normalizeInput, normalizeForSearch } from '../utils/text-normalization';

export default {
  name: 'ItemListPage',
  components: {
    ContentArea,
    CheckBox,
    MainButton,
    TextInput,
    ItemBox
  },
  data(): {
    listName: string;
    items: Item[];
    newItemName: string;
    searchQuery: string;
  } {
    return {
      listName: '',
      items: [],
      newItemName: '',
      searchQuery: ''
    };
  },
  created() {
    // ルートパラメータからリストIDを取得
    const listId = this.$route.params.id;
    // クエリパラメータからリスト名を取得
    const listName = this.$route.query.name as string | undefined;

    this.listName = listName || `リスト${listId}`;

    // TODO: APIからリストデータを取得
    console.log('リストID:', listId);
    console.log('リスト名:', this.listName);
  },
  computed: {
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
    addItem() {
      const normalizedName = normalizeText(this.newItemName);
      if (normalizedName) {
        this.items.push({
          id: Date.now(), //this to be replaced with unique ID from backend
          name: normalizedName,
          status: ItemStatus.PENDING
        });
        this.newItemName = '';
      }
    },
    // 入力時のリアルタイム正規化
    onItemNameInput(value: string): void {
      this.newItemName = normalizeInput(value);
    },
    onSearchInput(value: string): void {
      this.searchQuery = normalizeInput(value);
    },
    toggleItem(item: Item) {
      item.status = item.status === ItemStatus.COMPLETED ? ItemStatus.PENDING : ItemStatus.COMPLETED;
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
        <div class="px-2 py-2 border border-charcoal-200 bg-charcoal-100 rounded-md">
          <TextInput
            :model-value="searchQuery"
            @update:model-value="onSearchInput"
            input-name="search"
            placeholder="検索..."
            aria-label="アイテムを検索"
            variant="inline"
          />
        </div>
      </div>

      <!-- アイテムリスト -->
      <div class="space-y-3">
        <ItemBox v-for="item in filteredItems" :key="item.id" :item="item" @toggle="toggleItem" @delete="deleteItem" />

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
