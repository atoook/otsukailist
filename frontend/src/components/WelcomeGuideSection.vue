<script>
import { markRaw } from 'vue';
import IconCheck from './icons/IconCheck.vue';
import IconEdit from './icons/IconEdit.vue';
import IconUsers from './icons/IconUsers.vue';

const GUIDE_COPY = {
  eyebrow: '登録なしですぐ使える',
  title: '作って、送って、みんなで買う。',
  description:
    'はじめるを押したら、買い物リスト名とメンバーを決めるだけ。URLを共有すれば、同じリストを全員で確認できます。',
  noteTitle: 'BBQやまとめ買いにも。',
  noteDescription: '買うものと持参するものを一つのリストにまとめて、担当ごとの準備状況まで見やすく整理できます。'
};

const GUIDE_STEPS = [
  {
    stepLabel: 'STEP 1',
    title: 'リストを作る',
    description: 'タイトルと買い物メンバーを入力して、買い物リストの土台を作ります。',
    icon: markRaw(IconEdit)
  },
  {
    stepLabel: 'STEP 2',
    title: 'URLを送って一緒に編集',
    description: '家族や友だちにリンクを共有。みんなで品目を追加したり、担当を決めたりできます。',
    icon: markRaw(IconUsers)
  },
  {
    stepLabel: 'STEP 3',
    title: '買ったらチェック',
    description: '野菜売り場では野菜だけなど、カテゴリで絞り込みながらチェック。重複買いや買い忘れを減らせます。',
    icon: markRaw(IconCheck)
  }
];

export default {
  name: 'WelcomeGuideSection',
  data() {
    return {
      copy: GUIDE_COPY,
      steps: GUIDE_STEPS
    };
  }
};
</script>

<template>
  <section class="mt-12 border-t border-charcoal-200 pt-8" aria-labelledby="welcome-guide-title">
    <div class="mb-5">
      <p class="text-xs font-semibold text-wood-600 mb-2">{{ copy.eyebrow }}</p>
      <h2 id="welcome-guide-title" class="text-xl font-bold text-charcoal-800 leading-snug">
        {{ copy.title }}
      </h2>
      <p class="text-sm text-charcoal-600 leading-relaxed mt-2">
        {{ copy.description }}
      </p>
    </div>

    <ol class="space-y-3">
      <li v-for="step in steps" :key="step.stepLabel" class="bg-white border border-charcoal-200 rounded-lg p-4">
        <div class="flex gap-3">
          <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-wood-50 text-wood-600 text-lg">
            <component :is="step.icon" />
          </span>
          <div>
            <span class="text-xs font-semibold text-charcoal-400">{{ step.stepLabel }}</span>
            <h3 class="text-sm font-semibold text-charcoal-800 mt-1 mb-1">{{ step.title }}</h3>
            <p class="text-xs text-charcoal-600 leading-relaxed">
              {{ step.description }}
            </p>
          </div>
        </div>
      </li>
    </ol>

    <div class="mt-5 rounded-lg bg-wood-50 border border-wood-100 px-4 py-3">
      <p class="text-sm font-semibold text-charcoal-800">{{ copy.noteTitle }}</p>
      <p class="text-xs text-charcoal-600 leading-relaxed mt-1">
        {{ copy.noteDescription }}
      </p>
    </div>
  </section>
</template>
