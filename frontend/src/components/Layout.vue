<template>
  <div class="min-h-screen bg-gray-100 px-4 py-6 flex items-center">
    <div class="max-w-md mx-auto">
      <!-- メインカードコンテナ -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden h-[80vh] flex flex-col">
        <slot />
      </div>
    </div>
  </div>
</template>
