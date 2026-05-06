<template>
  <span class="relative inline-flex">
    <button
      type="button"
      class="rounded-full focus:outline-none focus:ring-2 focus:ring-wood-300 disabled:cursor-default disabled:opacity-60"
      :disabled="disabled"
      :aria-disabled="disabled"
      :aria-label="filterLabel"
      @click="handleFilter"
    >
      <BadgeTag :text="text" :size="size" :variant="variant" />
    </button>
    <IconButton
      v-if="active && !disabled"
      class="absolute -right-1.5 -top-1.5 border border-ember-200 bg-wood-50"
      variant="danger"
      size="tiny"
      :aria-label="clearLabel"
      @click.stop="$emit('clear')"
    >
      <IconClose />
    </IconButton>
  </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BadgeTag from './BadgeTag.vue';
import IconButton from './IconButton.vue';
import IconClose from './icons/IconClose.vue';

export default defineComponent({
  name: 'FilterBadgeButton',
  components: {
    BadgeTag,
    IconButton,
    IconClose
  },
  props: {
    text: {
      type: String,
      required: true
    },
    filterLabel: {
      type: String,
      required: true
    },
    clearLabel: {
      type: String,
      required: true
    },
    active: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: 'small',
      validator: (value: string) => ['small', 'default', 'large'].includes(value)
    },
    variant: {
      type: String,
      default: 'secondary',
      validator: (value: string) => ['default', 'primary', 'secondary'].includes(value)
    }
  },
  emits: ['filter', 'clear'],
  methods: {
    handleFilter(): void {
      if (this.disabled) {
        return;
      }
      if (this.active) {
        this.$emit('clear');
        return;
      }
      this.$emit('filter');
    }
  }
});
</script>
