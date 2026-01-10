<template>
  <input
    :id="inputId || undefined"
    :name="inputName || inputId"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
    @keydown="handleKeyDown"
    type="text"
    :placeholder="placeholder"
    :aria-label="ariaLabel || undefined"
    :disabled="disabled"
    :readonly="readonly"
    :maxlength="maxlength"
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
    inputName: {
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
    },
    ariaLabel: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    },
    maxlength: {
      type: Number,
      default: 255
    }
  },
  methods: {
    // エンター処理（event.isComposingで確実にIME制御）
    handleKeyDown(event) {
      if (event.key === 'Enter' && !event.isComposing) {
        event.preventDefault();
        this.$emit('enter');
      }
    }
  },
  computed: {
    inputClass() {
      const baseClass = [
        // Base styling shared by all variants
        'border rounded-lg',
        // Focus base
        'focus:outline-none',
        // Typography
        'text-charcoal-800 placeholder-charcoal-500'
      ].join(' ');

      const variantClasses = {
        default: [
          // Full width form input
          'w-full',
          // Padding for standalone input
          'px-4 py-3',
          // Colors
          'border-wood-300 bg-wood-50',
          // Focus with ring
          'focus:ring-2 focus:ring-wood-500 focus:border-wood-500'
        ].join(' '),
        inline: [
          // Flexible inline input (no padding - container provides it)
          'flex-1',
          // Minimal styling for inline use
          'border-transparent bg-transparent',
          // No focus ring for inline
          'focus:border-transparent'
        ].join(' ')
      };

      return twMerge(baseClass, variantClasses[this.variant]);
    }
  }
};
</script>
