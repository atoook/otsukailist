<template>
  <label :class="['relative w-6 h-6', disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer']">
    <!-- 実際のチェックボックス（操作主体） -->
    <input
      type="checkbox"
      :checked="checked"
      :disabled="disabled"
      :aria-label="ariaLabel"
      @change="$emit('toggle')"
      @keydown="$emit('keydown', $event)"
      class="sr-only"
    />
    <!-- 見た目のオーバーレイ（装飾のみ） -->
    <div
      :class="{
        'bg-wood-100 border-wood-300': !checked,
        'bg-ember-500 border-ember-600': checked
      }"
      class="w-6 h-6 border-2 rounded-md transition-[background-color,border-color,box-shadow] duration-200 flex items-center justify-center hover:shadow-md"
    >
      <!-- チェックマーク -->
      <span v-if="checked" class="text-white flex items-center"><IconCheck /></span>
    </div>
  </label>
</template>
<script>
import IconCheck from './icons/IconCheck.vue';

export default {
  name: 'CheckBox',
  components: {
    IconCheck
  },
  props: {
    checked: {
      type: Boolean,
      default: false
    },
    ariaLabel: {
      type: String,
      required: true
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle', 'keydown']
};
</script>
