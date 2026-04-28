<template>
  <span :class="badgeClass">
    <span v-if="$slots.icon" class="flex-shrink-0 flex items-center"><slot name="icon" /></span>
    <span class="overflow-hidden whitespace-nowrap text-ellipsis">{{ text }}</span>
    <IconButton
      v-if="removable"
      @click="$emit('remove')"
      class="-mr-1"
      variant="danger"
      size="xsmall"
      :aria-label="`${text}を削除`"
    >
      <IconClose />
    </IconButton>
  </span>
</template>

<script>
import { twMerge } from 'tailwind-merge';
import IconButton from './IconButton.vue';
import IconClose from './icons/IconClose.vue';

export default {
  name: 'BadgeTag',
  components: {
    IconButton,
    IconClose
  },
  inheritAttrs: false, // 自動的なattribute継承を無効化
  emits: ['remove'],
  props: {
    text: {
      type: String,
      required: true
    },
    removable: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['small', 'default', 'large'].includes(value)
    },
    variant: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'primary', 'secondary'].includes(value)
    }
  },
  computed: {
    badgeClass() {
      const baseClass = 'inline-flex items-center gap-2 rounded-full font-medium border transition-colors';

      const variantClasses = {
        default: 'bg-ember-100 border-ember-200 text-ember-700',
        primary: 'bg-wood-200 border-wood-300 text-charcoal-800',
        secondary: 'bg-wood-50 text-charcoal-800 border-wood-100'
      };

      const sizeClasses = {
        small: 'px-2 py-1 text-xs max-w-32',
        default: 'px-3 py-1 text-sm max-w-48',
        large: 'px-4 py-2 text-base max-w-64'
      };

      // 外部から渡されたクラスも含めてマージ
      return twMerge(baseClass, variantClasses[this.variant], sizeClasses[this.size], this.$attrs.class);
    }
  }
};
</script>
