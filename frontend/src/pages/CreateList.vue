<script>
import CardContent from '../components/CardContent.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'CreateList',
  components: {
    CardContent,
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
  <CardContent layout="center-vertical">
    <div class="w-full max-w-sm">
      <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">リスト名を設定</h2>

      <div class="mb-6">
        <label for="listName" class="block text-sm font-medium text-gray-700 mb-2"> リスト名 </label>
        <input
          id="listName"
          v-model="listName"
          type="text"
          placeholder="例：今日の買い物"
          class="w-full px-3 py-2 border-b border-gray-300 focus:outline-none"
        />
      </div>

      <div class="flex flex-col gap-3">
        <MainButton @click="createList" :disabled="!listName.trim()"> リストを作成 </MainButton>
      </div>
    </div>
  </CardContent>
</template>
