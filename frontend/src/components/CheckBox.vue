<template>
  <label class="relative w-6 h-6 cursor-pointer">
    <!-- 実際のチェックボックス（操作主体） -->
    <input
      type="checkbox"
      :checked="checked"
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
      class="w-6 h-6 border-2 rounded-md transition-[background-color,border-color,box-shadow] duration-200 text-center leading-6 hover:shadow-md"
    >
      <!-- チェックマーク -->
      <span v-if="checked" class="text-white text-sm font-bold align-top"> ✓ </span>
    </div>
  </label>
</template>
<script>
export default {
  name: 'CheckBox',
  props: {
    checked: {
      type: Boolean,
      default: false
    },
    ariaLabel: {
      type: String,
      required: true
    }
  },
  emits: ['toggle', 'keydown']
};
</script>
