<template>
  <div
    class="bg-gradient-to-r from-charcoal-700 via-charcoal-600 to-charcoal-700 text-wood-50 px-6 py-5 shadow-lg relative overflow-hidden"
  >
    <!-- 装飾要素 -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute top-2 left-4 w-2 h-2 bg-ember-400 rounded-full"></div>
      <div class="absolute top-6 right-8 w-1.5 h-1.5 bg-wood-200 rounded-full"></div>
      <div class="absolute bottom-4 left-12 w-1 h-1 bg-ember-400 rounded-full"></div>
      <div class="absolute bottom-2 right-4 w-2.5 h-2.5 bg-wood-300 rounded-full"></div>
    </div>

    <router-link to="/" class="block relative z-10">
      <div class="text-center">
        <!-- BBQアイコン -->
        <div class="mb-2">
          <span class="text-3xl">🔥</span>
        </div>
        <!-- タイトル -->
        <h1
          class="text-xl font-bold font-serif tracking-wide hover:text-wood-200 transition-all duration-300 cursor-pointer transform hover:scale-105"
        >
          <span class="bg-clip-text text-transparent bg-gradient-to-r from-wood-200 to-ember-400"> OTSUKAI </span>
          <span class="text-wood-100 ml-1">リスト</span>
        </h1>
        <!-- サブタイトル -->
        <p class="text-xs text-wood-200 mt-1 font-medium">あなたの買い物をスモーキーに</p>
      </div>
    </router-link>
  </div>
</template>
