<script>
export default {
  name: 'LoadingSpinner',
  props: {
    message: {
      type: String,
      default: ''
    }
  }
};
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-4" role="status" aria-live="polite" aria-busy="true">
    <div
      class="w-10 h-10 rounded-full border-4 border-wood-200 border-t-wood-500 animate-spin"
      aria-hidden="true"
    ></div>
    <span class="sr-only">{{ message || 'Loading…' }}</span>
    <p v-if="message" class="text-sm text-charcoal-500" aria-hidden="true">{{ message }}</p>
  </div>
</template>
