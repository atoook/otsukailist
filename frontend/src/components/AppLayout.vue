<script>
export default {
  name: 'AppLayout'
};
</script>

<template>
  <div class="min-h-screen bg-charcoal-200">
    <div class="min-h-screen max-w-sm align-center flex flex-col bg-wood-50">
      <!-- メインコンテナ -->
      <slot />
    </div>
  </div>
</template>
