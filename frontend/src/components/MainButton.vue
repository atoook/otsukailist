<template>
  <button
    type="button"
    :disabled="disabled"
    class="bg-wood-500 text-wood-50 px-6 py-3 rounded-lg hover:bg-wood-600 active:bg-wood-700 transition-all duration-200 transform hover:scale-105 shadow-md hover:shadow-lg border border-wood-600 font-medium disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
  >
    <slot />
  </button>
</template>

<script>
export default {
  name: 'MainButton',
  props: {
    disabled: {
      type: Boolean,
      default: false
    }
  }
};
</script>
