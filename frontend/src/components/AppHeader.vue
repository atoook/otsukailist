<template>
  <div
    class="bg-gradient-to-r from-charcoal-700 via-charcoal-600 to-charcoal-700 text-wood-50 shadow-lg relative overflow-hidden h-16"
  >
    <!-- 装飾要素 -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute top-2 left-4 w-2 h-2 bg-ember-400 rounded-full"></div>
      <div class="absolute top-6 right-8 w-1.5 h-1.5 bg-wood-200 rounded-full"></div>
      <div class="absolute bottom-4 left-12 w-1 h-1 bg-ember-400 rounded-full"></div>
      <div class="absolute bottom-2 right-4 w-2.5 h-2.5 bg-wood-300 rounded-full"></div>
    </div>

    <router-link to="/" class="flex items-center justify-center h-full relative z-10">
      <img
        :src="logoImage"
        alt="Otsukai リスト"
        class="h-36 w-auto object-cover transition-transform duration-300 hover:scale-105 translate-y-2"
      />
    </router-link>
  </div>
</template>

<script>
import logoImage from '../assets/Header_Title.png';

export default {
  name: 'AppHeader',
  data() {
    return { logoImage };
  }
};
</script>
