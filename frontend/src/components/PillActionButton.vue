<template>
  <button type="button" :class="buttonClass" :disabled="disabled">
    <span v-if="$slots.icon" class="flex shrink-0 items-center" aria-hidden="true">
      <slot name="icon" />
    </span>
    <span class="min-w-0 overflow-hidden text-ellipsis whitespace-nowrap">
      <slot />
    </span>
  </button>
</template>

<script>
import { twMerge } from 'tailwind-merge';

export default {
  name: 'PillActionButton',
  props: {
    disabled: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    buttonClass() {
      const baseClass = [
        'inline-flex items-center justify-center gap-1 rounded-full border border-wood-200 bg-white',
        'px-2.5 py-1.5 text-xs font-medium text-charcoal-600 shadow-sm',
        'transition-[background-color,border-color,box-shadow]',
        'hover:border-wood-300 hover:bg-wood-50 hover:text-charcoal-800 hover:shadow',
        'focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300',
        'disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:border-wood-200 disabled:hover:bg-white disabled:hover:shadow-sm'
      ].join(' ');

      return twMerge(baseClass, this.$attrs.class);
    }
  }
};
</script>
