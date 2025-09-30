<script>
import CardContent from '../components/CardContent.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'ItemList',
  components: {
    CardContent,
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
  <CardContent>
    <div class="w-full">
      <!-- リストタイトル -->
      <div class="mb-6">
        <h2 class="text-2xl font-bold text-gray-800 text-center mb-2">{{ listName }}</h2>
        <p class="text-sm text-gray-500 text-center">買い物リスト</p>
      </div>

      <!-- 新しいアイテム追加 -->
      <div class="mb-6">
        <div class="flex gap-2 px-1.5 py-1.5 border border-gray-300 rounded-lg">
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
        <div v-for="item in items" :key="item.id" class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
          <!-- チェックボックス -->
          <input type="checkbox" :checked="item.completed" @change="toggleItem(item)" class="w-5 h-5 text-blue-600" />

          <!-- アイテム名 -->
          <span
            :class="{
              'line-through text-gray-500': item.completed,
              'text-gray-900': !item.completed
            }"
            class="flex-1"
          >
            {{ item.name }}
          </span>

          <!-- 削除ボタン -->
          <button @click="deleteItem(item.id)" class="text-red-500 hover:text-red-700 text-sm">削除</button>
        </div>

        <!-- アイテムがない場合 -->
        <div v-if="items.length === 0" class="text-center text-gray-500 py-8">
          まだアイテムがありません。<br />
          上のフォームからアイテムを追加してください。
        </div>
      </div>
    </div>
  </CardContent>
</template>
