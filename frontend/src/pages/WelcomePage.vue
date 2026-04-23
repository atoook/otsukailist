<script>
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import SwipeContainer from '../components/SwipeContainer.vue';
import BadgeTag from '../components/BadgeTag.vue';
import IconFire from '../components/icons/IconFire.vue';
import IconPray from '../components/icons/IconPray.vue';
import IconClipboard from '../components/icons/IconClipboard.vue';
import { getListHistory, removeListHistoryEntry } from '@/lib/userCache';
import { fetchListsMeta } from '@/api/list';
import { FEEDBACK_URL } from '@/lib/appConstants';

export default {
  name: 'WelcomePage',
  components: {
    ContentArea,
    MainButton,
    SwipeContainer,
    BadgeTag,
    IconFire,
    IconPray,
    IconClipboard
  },
  data() {
    return {
      listHistory: [],
      listMeta: {},
      metaLoading: false,
      feedbackUrl: FEEDBACK_URL
    };
  },
  async created() {
    this.listHistory = getListHistory();
    await this.loadMeta();
  },
  methods: {
    async loadMeta() {
      if (this.listHistory.length === 0) return;
      this.metaLoading = true;
      try {
        const ids = this.listHistory.map((e) => e.listId);
        const metaList = await fetchListsMeta(ids);

        const metaMap = {};
        for (const m of metaList) {
          metaMap[m.listId] = m;
        }
        this.listMeta = metaMap;

        // 返ってこなかったIDは削除済みとみなしてlocalStorageから除去
        const returnedIds = new Set(metaList.map((m) => m.listId));
        const removed = ids.filter((id) => !returnedIds.has(id));
        for (const id of removed) {
          removeListHistoryEntry(id);
        }
        if (removed.length > 0) {
          this.listHistory = this.listHistory.filter((e) => returnedIds.has(e.listId));
        }
      } catch (error) {
        // メタ取得失敗時はキャッシュのリスト名のみ表示継続（UX劣化なし）
        console.error('Failed to load list metadata', error);
      } finally {
        this.metaLoading = false;
      }
    },
    navigateToCreateList() {
      this.$router.push('/create-list');
    },
    removeHistoryEntry(listId) {
      removeListHistoryEntry(listId);
      this.listHistory = this.listHistory.filter((e) => e.listId !== listId);
    },
    formatLastActivity(isoString) {
      if (!isoString) return null;
      const date = new Date(isoString);
      if (isNaN(date.getTime())) return null;
      const now = new Date();
      const diffMs = Math.max(0, now.getTime() - date.getTime());
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
      if (diffDays === 0) {
        return date.toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
      } else if (diffDays < 7) {
        return `${diffDays}日前`;
      } else {
        return date.toLocaleDateString('ja-JP', { month: 'numeric', day: 'numeric' });
      }
    }
  },
  computed: {
    formattedListMeta() {
      const result = {};
      for (const [listId, meta] of Object.entries(this.listMeta)) {
        result[listId] = {
          ...meta,
          formattedActivity: this.formatLastActivity(meta.lastItemActivityAt)
        };
      }
      return result;
    }
  }
};
</script>

<template>
  <ContentArea>
    <div class="text-center">
      <div class="text-6xl mb-4 flex justify-center"><IconFire /></div>
      <h2 class="text-3xl font-bold text-charcoal-800 mb-4">ようこそ！</h2>
      <p class="text-charcoal-600 mb-8 leading-relaxed">あなたの買い物を<br />スマートに管理しましょう</p>
      <MainButton @click="navigateToCreateList">はじめる</MainButton>
    </div>

    <!-- フィードバックリンク -->
    <div class="mt-8 text-center">
      <p class="text-xs text-charcoal-500">
        ご意見・ご感想は
        <a
          :href="feedbackUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="underline hover:text-charcoal-700"
          aria-label="フィードバックリストへ（別タブで開きます）"
          >フィードバックリストへ</a
        >
        どうぞ <IconPray />
      </p>
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
              <span class="text-charcoal-400 mr-3 text-base flex items-center"><IconClipboard /></span>
              <span class="text-sm text-charcoal-700 font-medium truncate flex-1">
                {{ listMeta[entry.listId]?.name ?? entry.name }}
              </span>
              <span class="flex items-center gap-2 ml-2 shrink-0">
                <template v-if="metaLoading">
                  <span class="text-xs text-charcoal-300 animate-pulse">···</span>
                </template>
                <template v-else-if="formattedListMeta[entry.listId]">
                  <span class="text-xs text-charcoal-500">
                    {{ formattedListMeta[entry.listId].incompleteCount }}/{{
                      formattedListMeta[entry.listId].itemCount
                    }}件
                  </span>
                  <span v-if="formattedListMeta[entry.listId].formattedActivity" class="text-xs text-charcoal-400">
                    {{ formattedListMeta[entry.listId].formattedActivity }}
                  </span>
                </template>
              </span>
              <span class="text-charcoal-300 text-xs ml-2" aria-hidden="true">›</span>
            </router-link>
            <template #hiddenActions>
              <button
                type="button"
                @click="removeHistoryEntry(entry.listId)"
                :aria-label="`${entry.name}を履歴からクリア`"
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
