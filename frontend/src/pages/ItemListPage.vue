<script>
import ContentArea from '../components/ContentArea.vue';
import CheckBox from '../components/CheckBox.vue';
import MainButton from '../components/MainButton.vue';
import TextInput from '../components/TextInput.vue';

export default {
  name: 'ItemListPage',
  components: {
    ContentArea,
    CheckBox,
    MainButton,
    TextInput
  },
  data() {
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
    const listName = this.$route.query.name;

    this.listName = listName || `リスト${listId}`;

    // TODO: APIからリストデータを取得
    console.log('リストID:', listId);
    console.log('リスト名:', this.listName);
  },
  computed: {
    filteredItems() {
      // Ensure items is an array
      const items = Array.isArray(this.items) ? this.items : [];

      if (!this.searchQuery.trim()) {
        return items;
      }
      const query = this.searchQuery.toLowerCase();
      return items.filter((item) => {
        // Guard against null/undefined item.name
        const itemName = item?.name || '';
        return itemName.toLowerCase().includes(query);
      });
    }
  },
  methods: {
    addItem() {
      if (this.newItemName.trim()) {
        this.items.push({
          id: Date.now(),
          name: this.newItemName,
          completed: false
        });
        this.newItemName = '';
      }
    },
    toggleItem(item) {
      item.completed = !item.completed;
    },
    deleteItem(itemId) {
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
            v-model="newItemName"
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
            v-model="searchQuery"
            input-name="search"
            placeholder="検索..."
            aria-label="アイテムを検索"
            variant="inline"
            class="text-sm"
          />
        </div>
      </div>

      <!-- アイテムリスト -->
      <div class="space-y-3">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="flex items-center gap-3 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm"
        >
          <!-- カスタムチェックボックス -->
          <CheckBox
            :checked="item.completed"
            :aria-label="`${item.name}を完了としてマーク`"
            @toggle="toggleItem(item)"
          />

          <!-- アイテム名 -->
          <span
            :class="{
              'line-through text-charcoal-500': item.completed,
              'text-charcoal-800': !item.completed
            }"
            class="flex-1"
          >
            {{ item.name }}
          </span>

          <!-- 削除ボタン -->
          <button
            @click="deleteItem(item.id)"
            class="text-ember-500 hover:text-ember-600 text-sm font-medium transition-colors"
          >
            🗑️
          </button>
        </div>

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
