<template>
  <div>
    <label :for="inputId" :class="labelClass">
      {{ label }}
    </label>
    <TextInput
      :input-id="inputId"
      :placeholder="placeholder"
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      @enter="$emit('enter')"
      :variant="inputVariant"
    />
  </div>
</template>

<script>
import { twMerge } from 'tailwind-merge';
import TextInput from './TextInput.vue';

export default {
  name: 'TextInputWithLabel',
  components: {
    TextInput
  },
  emits: ['update:modelValue', 'enter'],
  props: {
    inputId: {
      type: String,
      required: true
    },
    label: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      required: true
    },
    modelValue: {
      type: String,
      default: ''
    },
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'compact', 'large'].includes(value)
    }
  },
  computed: {
    labelClass() {
      const baseClass = 'block font-medium text-charcoal-700';

      const variantClasses = {
        default: 'text-sm mb-2',
        compact: 'text-xs mb-1',
        large: 'text-base mb-3'
      };

      return twMerge(baseClass, variantClasses[this.variant]);
    },
    inputVariant() {
      // TextInputのバリアントマッピング
      const inputVariantMap = {
        default: 'default',
        compact: 'default', // 入力フィールド自体は同じ
        large: 'default' // 入力フィールド自体は同じ
      };

      return inputVariantMap[this.variant];
    }
  }
};
</script>
