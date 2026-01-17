<script>
export default {
  name: 'AppLayout'
};
</script>

<template>
  <div class="min-h-[100dvh] bg-charcoal-200">
    <div class="min-h-[100dvh] max-w-sm mx-auto flex flex-col bg-wood-50">
      <!-- メインコンテナ -->
      <slot />
    </div>
  </div>
</template>
