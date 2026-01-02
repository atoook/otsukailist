<template>
  <div @click="$emit('toggle')" class="relative w-6 h-6 cursor-pointer">
    <!-- 隠れたチェックボックス -->
    <input type="checkbox" :checked="checked" class="sr-only" />
    <!-- カスタムチェックボックス本体 -->
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
  </div>
</template>
<script>
export default {
  name: 'CheckBox',
  props: {
    checked: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle']
};
</script>
