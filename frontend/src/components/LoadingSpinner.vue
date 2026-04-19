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
  <div class="flex flex-col items-center justify-center gap-4">
    <div class="w-10 h-10 rounded-full border-4 border-wood-200 border-t-wood-500 animate-spin"></div>
    <p v-if="message" class="text-sm text-charcoal-500">{{ message }}</p>
  </div>
</template>
