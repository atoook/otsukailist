<script>
export default {
  props: {
    layout: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'center', 'center-vertical'].includes(value)
    },
    minHeight: {
      type: String,
      default: 'min-h-96'
    }
  },
  computed: {
    containerClass() {
      const baseClass = `p-6 overflow-y-auto flex-1`;

      switch (this.layout) {
        case 'center':
          return `${baseClass} flex items-center justify-center`;
        case 'center-vertical':
          return `${baseClass} flex items-center`;
        default:
          return baseClass;
      }
    }
  }
};
</script>

<template>
  <div :class="containerClass">
    <slot />
  </div>
</template>
