<template>
  <input
    :id="inputId"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
    @keyup.enter="$emit('enter')"
    type="text"
    :placeholder="placeholder"
    :class="inputClass"
  />
</template>

<script>
import { twMerge } from 'tailwind-merge';

export default {
  name: 'TextInput',
  emits: ['update:modelValue', 'enter'],
  props: {
    inputId: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    modelValue: {
      type: String,
      default: ''
    },
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'inline'].includes(value)
    }
  },
  computed: {
    inputClass() {
      const baseClass = [
        // Base styling
        'px-4 py-3 border rounded-lg',
        // Focus states
        'focus:outline-none focus:ring-2',
        // Typography
        'text-charcoal-800 placeholder-charcoal-500'
      ].join(' ');

      const variantClasses = {
        default: [
          // Full width form input
          'w-full',
          // Colors
          'border-wood-300 bg-wood-50',
          // Focus colors
          'focus:ring-wood-500 focus:border-wood-500'
        ].join(' '),
        inline: [
          // Flexible inline input
          'flex-1',
          // Minimal styling for inline use
          'border-transparent bg-transparent',
          // No focus ring for inline
          'focus:ring-0 focus:border-transparent'
        ].join(' ')
      };

      return twMerge(baseClass, variantClasses[this.variant]);
    }
  }
};
</script>
