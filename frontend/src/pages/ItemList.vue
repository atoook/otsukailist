<script>
import ContentArea from '../components/ContentArea.vue';
import CheckBox from '../components/CheckBox.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'ItemList',
  components: {
    ContentArea,
    CheckBox,
    MainButton
  },
  data() {
    return {
      listName: '',
      items: [],
      newItemName: ''
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
          <input
            v-model="newItemName"
            @keyup.enter="addItem"
            type="text"
            placeholder="アイテムを追加..."
            class="flex-1 focus:outline-none"
          />
          <MainButton @click="addItem" :disabled="!newItemName.trim()"> 追加 </MainButton>
        </div>
      </div>

      <!-- アイテムリスト -->
      <div class="space-y-3">
        <div
          v-for="item in items"
          :key="item.id"
          class="flex items-center gap-3 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm"
        >
          <!-- カスタムチェックボックス -->
          <CheckBox :checked="item.completed" @toggle="toggleItem(item)" />

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
      </div>
    </div>
  </ContentArea>
</template>
