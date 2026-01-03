<script>
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'WelcomePage',
  components: {
    ContentArea,
    MainButton
  }
};
</script>

<template>
  <ContentArea>
    <div class="text-center">
      <div class="text-6xl mb-4">🔥</div>
      <h2 class="text-3xl font-bold font-serif text-charcoal-800 mb-4">ようこそ！</h2>
      <p class="text-charcoal-600 mb-8 leading-relaxed">あなたの買い物を<br />🍖 スマートに管理しましょう</p>
      <router-link to="/create-list">
        <MainButton>はじめる</MainButton>
      </router-link>
    </div>
  </ContentArea>
</template>
