<script lang="ts">
import { defineComponent } from 'vue';

const SIZE_CLASSES = {
  xs: 'h-3 w-3 border-2',
  sm: 'h-4 w-4 border-2',
  md: 'h-5 w-5 border-2',
  lg: 'h-10 w-10 border-4'
} as const;

const TONE_CLASSES = {
  wood: 'border-wood-300 border-t-ember-600',
  primary: 'border-wood-50/50 border-t-wood-50',
  inverse: 'border-white/50 border-t-white',
  page: 'border-wood-200 border-t-wood-500'
} as const;

type SpinnerSize = keyof typeof SIZE_CLASSES;
type SpinnerTone = keyof typeof TONE_CLASSES;

export default defineComponent({
  name: 'InlineSpinner',
  props: {
    size: {
      type: String as () => SpinnerSize,
      default: 'sm',
      validator: (value: string) => Object.keys(SIZE_CLASSES).includes(value)
    },
    tone: {
      type: String as () => SpinnerTone,
      default: 'wood',
      validator: (value: string) => Object.keys(TONE_CLASSES).includes(value)
    }
  },
  computed: {
    spinnerClass(): string {
      return ['inline-block shrink-0 rounded-full animate-spin', SIZE_CLASSES[this.size], TONE_CLASSES[this.tone]].join(
        ' '
      );
    }
  }
});
</script>

<template>
  <span :class="spinnerClass" aria-hidden="true"></span>
</template>
