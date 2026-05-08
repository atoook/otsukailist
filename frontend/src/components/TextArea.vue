<template>
  <textarea
    ref="textarea"
    :id="inputId || undefined"
    :name="inputName || inputId"
    :value="modelValue"
    :placeholder="placeholder"
    :aria-label="ariaLabel || undefined"
    :disabled="disabled"
    :readonly="readonly"
    :maxlength="maxlength"
    :rows="rows"
    wrap="soft"
    :class="textareaClass"
    @input="handleInput"
    @keydown="handleKeyDown"
  ></textarea>
</template>

<script>
import { twMerge } from 'tailwind-merge';

export default {
  name: 'TextArea',
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
      validator: (value) => ['default'].includes(value)
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
    },
    rows: {
      type: Number,
      default: 3
    },
    autoResize: {
      type: Boolean,
      default: false
    },
    preventEnter: {
      type: Boolean,
      default: false
    },
    extraClass: {
      type: String,
      default: ''
    }
  },
  mounted() {
    this.adjustHeight();
  },
  updated() {
    this.adjustHeight();
  },
  methods: {
    handleInput(event) {
      this.$emit('update:modelValue', event.target.value);
      this.$nextTick(() => this.adjustHeight());
    },
    handleKeyDown(event) {
      if (event.key === 'Enter' && !event.isComposing) {
        if (this.preventEnter) {
          event.preventDefault();
        }
        this.$emit('enter');
      }
    },
    adjustHeight() {
      if (!this.autoResize) {
        return;
      }
      const textarea = this.$refs.textarea;
      if (!textarea) {
        return;
      }
      textarea.style.height = 'auto';
      textarea.style.height = `${textarea.scrollHeight}px`;
    }
  },
  computed: {
    textareaClass() {
      const baseClass = [
        'border rounded-lg',
        'focus:outline-none',
        'text-base leading-6 text-charcoal-800 placeholder-charcoal-500'
      ].join(' ');

      const variantClasses = {
        default: [
          'w-full',
          'resize-none overflow-y-auto',
          'px-4 py-3',
          'border-wood-300 bg-wood-50',
          'focus:ring-2 focus:ring-wood-500 focus:border-wood-500'
        ].join(' ')
      };

      return twMerge(baseClass, variantClasses[this.variant], this.extraClass);
    }
  }
};
</script>
