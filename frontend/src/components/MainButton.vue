<template>
  <button type="button" :class="buttonClass" :disabled="disabled">
    <slot />
  </button>
</template>

<script>
import { twMerge } from 'tailwind-merge';

export default {
  name: 'MainButton',
  props: {
    variant: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'cancel'].includes(value)
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    buttonClass() {
      const baseClass = [
        // Layout & spacing
        'px-6 py-3 rounded-lg',
        // Typography
        'font-medium',
        // Transitions
        'transition-[background-color,transform,box-shadow] duration-200',
        // Transform & hover effects
        'transform hover:scale-105',
        // Shadows
        'shadow-md hover:shadow-lg',
        // Border
        'border',
        // Disabled states
        'disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none'
      ].join(' ');

      const variantClasses = {
        primary: 'bg-wood-500 text-wood-50 border-wood-600 hover:bg-wood-600 active:bg-wood-700',
        secondary: 'bg-transparent text-wood-700 border-wood-300 hover:bg-wood-50 active:bg-wood-100',
        cancel: 'bg-transparent text-charcoal-500 border-charcoal-300 hover:bg-charcoal-50 active:bg-charcoal-100'
      };

      return twMerge(baseClass, variantClasses[this.variant]);
    }
  }
};
</script>
