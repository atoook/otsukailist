<template>
  <div class="min-h-[100dvh] bg-charcoal-200 flex items-center justify-center">
    <div class="min-h-[100dvh] max-w-sm mx-auto flex flex-col w-full bg-wood-50 items-center justify-center">
      <div class="flex flex-col items-center p-8 rounded-lg shadow-lg w-full bg-white/90 border border-charcoal-100">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-16 w-16 text-ember-400 mb-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z"
          />
        </svg>
        <h1 class="text-2xl font-bold mb-2 text-charcoal-700">メンテナンス中</h1>
        <p class="text-charcoal-500 mb-4 text-center">
          ご不便をおかけしますが、ただいまメンテナンス作業中です。<br />しばらくお待ちください。
        </p>
        <span class="text-xs text-charcoal-300">Otsukaiリスト</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MaintenancePage'
};
</script>

<style scoped></style>
