<template>
  <button type="button" :class="buttonClass" :disabled="disabled" v-bind="buttonAttrs">
    <span :class="iconClass" aria-hidden="true">
      <slot />
    </span>
  </button>
</template>

<script>
import { twMerge } from 'tailwind-merge';

export default {
  name: 'IconButton',
  inheritAttrs: false,
  props: {
    variant: {
      type: String,
      default: 'ghost',
      validator: (value) => ['ghost', 'wood', 'muted', 'danger'].includes(value)
    },
    size: {
      type: String,
      default: 'small',
      validator: (value) => ['xsmall', 'small', 'medium'].includes(value)
    },
    disabled: {
      type: Boolean,
      default: false
    },
    active: {
      type: Boolean,
      default: false
    },
    iconClass: {
      type: [String, Object, Array],
      default: ''
    }
  },
  computed: {
    buttonAttrs() {
      const attrs = { ...this.$attrs };
      delete attrs.class;
      return attrs;
    },
    buttonClass() {
      const baseClass = [
        'inline-flex shrink-0 items-center justify-center rounded',
        'transition-[background-color,color,box-shadow,opacity] duration-200',
        'focus:outline-none focus:ring-2',
        'disabled:cursor-not-allowed disabled:opacity-40'
      ].join(' ');

      const sizeClasses = {
        xsmall: 'h-6 w-6 text-xs',
        small: 'h-8 w-8 text-sm',
        medium: 'h-10 w-10 text-base'
      };

      const variantClasses = {
        ghost: 'text-charcoal-700 hover:bg-charcoal-100 focus:ring-charcoal-400',
        wood: 'text-charcoal-700 hover:bg-wood-200 focus:ring-wood-300',
        muted: 'text-charcoal-400 hover:bg-charcoal-100 hover:text-charcoal-600 focus:ring-charcoal-300',
        danger: 'text-ember-500 hover:bg-ember-100 hover:text-ember-700 focus:ring-ember-300'
      };

      const activeClasses = {
        ghost: 'bg-charcoal-100 text-charcoal-800',
        wood: 'bg-wood-200 text-charcoal-800',
        muted: 'bg-charcoal-100 text-charcoal-600',
        danger: 'bg-ember-100 text-ember-700'
      };

      return twMerge(
        baseClass,
        sizeClasses[this.size],
        variantClasses[this.variant],
        this.active ? activeClasses[this.variant] : '',
        this.$attrs.class
      );
    }
  }
};
</script>
