<template>
  <div
    class="fixed bottom-0 left-1/2 z-10 box-border w-full max-w-sm -translate-x-1/2 border-t border-wood-200 bg-wood-50/95 px-6 py-3 backdrop-blur"
  >
    <div class="mx-auto flex w-full min-w-0 gap-3">
      <MainButton
        v-bind="$attrs"
        class="w-full min-w-0 whitespace-normal text-center"
        :disabled="disabled"
        :size="size"
        :variant="variant"
      >
        <slot />
      </MainButton>
    </div>
  </div>
</template>

<script>
import MainButton from './MainButton.vue';

export default {
  name: 'FixedBottomActionButton',
  components: { MainButton },
  inheritAttrs: false,
  props: {
    disabled: {
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
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'cancel', 'success'].includes(value)
    }
  }
};
</script>
