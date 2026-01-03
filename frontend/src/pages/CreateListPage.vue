<script>
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'CreateListPage',
  components: {
    ContentArea,
    MainButton
  },
  data() {
    return {
      listName: ''
    };
  },
  methods: {
    createList() {
      if (this.listName.trim()) {
        // リストIDを生成（実際のプロジェクトではAPIから取得）
        const listId = Date.now().toString();

        // TODO: APIでリストを作成
        console.log('リスト名:', this.listName);
        console.log('リストID:', listId);

        // アイテムリスト画面に遷移
        this.$router.push({
          name: 'ItemList',
          params: { id: listId },
          query: { name: this.listName }
        });
      }
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
      <label for="listName" class="block text-sm font-medium text-charcoal-700 mb-2"> 🍖 リスト名 </label>
      <input
        id="listName"
        v-model="listName"
        type="text"
        placeholder="例：今日のBBQ材料"
        class="w-full px-4 py-3 border border-wood-300 bg-wood-50 rounded-lg focus:outline-none focus:ring-2 focus:ring-wood-500 focus:border-wood-500 text-charcoal-800 placeholder-charcoal-500"
      />
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="createList" :disabled="!listName.trim()"> リストを作成 </MainButton>
    </div>
  </ContentArea>
</template>
