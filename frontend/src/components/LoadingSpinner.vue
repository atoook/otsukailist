<script>
import InlineSpinner from './InlineSpinner.vue';

export default {
  name: 'LoadingSpinner',
  components: { InlineSpinner },
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
    <InlineSpinner size="lg" tone="page" />
    <span class="sr-only">{{ message || 'Loading…' }}</span>
    <p v-if="message" class="text-sm text-charcoal-500" aria-hidden="true">{{ message }}</p>
  </div>
</template>
