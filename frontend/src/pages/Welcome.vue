<script>
import CardContent from '../components/CardContent.vue';
import MainButton from '../components/MainButton.vue';

export default {
  name: 'Welcome',
  components: {
    CardContent,
    MainButton
  }
};
</script>

<template>
  <CardContent layout="center">
    <div class="w-full max-w-sm text-center">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">ようこそ！</h2>
      <p class="text-gray-600 mb-6">おつかいリストを作成・管理できます</p>
      <router-link to="/create-list">
        <MainButton>はじめる</MainButton>
      </router-link>
    </div>
  </CardContent>
</template>
