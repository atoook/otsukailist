<script setup>
defineOptions({
  name: 'AppFooter'
});

const year = new Date().getFullYear();
</script>

<template>
  <div class="bg-gray-100 px-6 py-3">
    <nav class="mb-2 flex justify-center gap-4 text-xs text-gray-500" aria-label="フッターリンク">
      <router-link to="/terms" class="underline hover:text-gray-700">利用規約</router-link>
      <router-link to="/privacy" class="underline hover:text-gray-700">プライバシーポリシー</router-link>
    </nav>
    <p class="text-xs text-gray-500 text-center">© {{ year }} OTSUKAI リスト</p>
  </div>
</template>
