<script>
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import SwipeContainer from '../components/SwipeContainer.vue';
import BadgeTag from '../components/BadgeTag.vue';
import { getListHistory, removeListHistoryEntry } from '../lib/userCache';

export default {
  name: 'WelcomePage',
  components: {
    ContentArea,
    MainButton,
    SwipeContainer,
    BadgeTag
  },
  data() {
    return {
      listHistory: []
    };
  },
  created() {
    this.listHistory = getListHistory();
  },
  methods: {
    navigateToCreateList() {
      this.$router.push('/create-list');
    },
    removeHistoryEntry(listId) {
      removeListHistoryEntry(listId);
      this.listHistory = this.listHistory.filter((e) => e.listId !== listId);
    }
  }
};
</script>

<template>
  <ContentArea>
    <div class="text-center">
      <div class="text-6xl mb-4">🔥</div>
      <h2 class="text-3xl font-bold font-serif text-charcoal-800 mb-4">ようこそ！</h2>
      <p class="text-charcoal-600 mb-8 leading-relaxed">あなたの買い物を<br />🍖 スマートに管理しましょう</p>
      <MainButton @click="navigateToCreateList">はじめる</MainButton>
    </div>

    <!-- 最近見たリスト -->
    <div v-if="listHistory.length > 0" class="mt-10">
      <div class="flex items-center mb-3">
        <h3 class="text-sm font-semibold text-charcoal-600">最近見たリスト</h3>
      </div>
      <ul class="space-y-2">
        <li v-for="entry in listHistory" :key="entry.listId" class="rounded-lg overflow-hidden">
          <SwipeContainer hidden-bg-color="#fef7f0">
            <router-link
              :to="`/lists/${entry.listId}`"
              class="flex items-center px-4 py-3 bg-white border border-charcoal-200 rounded-lg hover:bg-charcoal-50 transition-colors"
            >
              <span class="text-charcoal-400 mr-3 text-base" aria-hidden="true">📋</span>
              <span class="text-sm text-charcoal-700 font-medium truncate flex-1">{{ entry.name }}</span>
              <span class="text-charcoal-300 text-xs ml-2" aria-hidden="true">›</span>
            </router-link>
            <template #hiddenActions>
              <button
                type="button"
                @click="removeHistoryEntry(entry.listId)"
                :aria-label="`${entry.name}を履歴からクリア`"
                tabindex="-1"
                role="button"
              >
                <BadgeTag text="履歴からクリア" size="small" class="bg-ember-400 border-ember-600 text-white" />
              </button>
            </template>
          </SwipeContainer>
        </li>
      </ul>
    </div>
  </ContentArea>
</template>
