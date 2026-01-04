<script>
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'ShareListPage',
  components: {
    ContentArea,
    MainButton
  },
  data() {
    return {
      listName: '',
      listId: '',
      listURL: ''
    };
  },
  created() {
    this.listName = this.$route.query.name;
    this.listId = this.$route.params.id;
    // ルートパラメータからリストIDを取得
    this.listURL = `http://example.com/lists/${this.listId}?name=${encodeURIComponent(this.listName)}`;
  },
  methods: {
    copyUrl() {
      navigator.clipboard
        .writeText(this.listURL)
        .then(() => {
          alert('リンクがコピーされました！');
        })
        .catch(() => {
          alert('コピーに失敗しました。手動でコピーしてください。');
        });
    },
    navigateToItemList() {
      this.$router.push({
        name: 'ItemList',
        params: { id: this.listId },
        query: { name: this.listName }
      });
    }
  }
};
</script>

<template>
  <ContentArea layout="center">
    <h1 class="text-2xl font-bold font-sans">共有リンク</h1>
    <div class="mt-6 flex gap-2 px-2 py-1 border border-charcoal-200 bg-charcoal-100 rounded-md w-full">
      <span
        class="flex-1 min-w-0 text-charcoal-800 whitespace-nowrap overflow-hidden text-clip overflow-x-scroll scrollbar-hidden"
      >
        {{ listURL }}
      </span>
      <MainButton @click="copyUrl" :disabled="!listURL" size="small"> コピー </MainButton>
    </div>
    <div class="mt-6">
      <MainButton variant="secondary" @click="navigateToItemList"> アイテムリストへ移動 </MainButton>
    </div>
  </ContentArea>
</template>
