<template>
  <button
    type="button"
    @pointerdown="handlePointerStart($event)"
    @pointerup="handlePointerEnd($event)"
    @pointercancel="handlePointerCancel"
    @lostpointercapture="handlePointerCancel"
    @click="handleClick($event)"
  >
    <slot />
  </button>
</template>

<script lang="ts">
// pointerup 直後に合成 click が続く場合があるため、短時間だけ click 側を抑止する。
const CLICK_SUPPRESSION_MS = 500;

export default {
  name: 'SwipeContainerAction',
  emits: ['activate'],
  data() {
    return {
      pointerStarted: false,
      lastPointerActivateAt: 0
    };
  },
  methods: {
    // モバイルではスワイプ直後の tap で click が生成されないことがあるため、pointerup で activate する。
    isPrimaryPointer(event: PointerEvent) {
      return event.isPrimary !== false && event.button === 0;
    },
    isPrimaryClick(event: MouseEvent) {
      return event.button === 0;
    },
    handlePointerStart(event: PointerEvent) {
      if (!this.isPrimaryPointer(event)) {
        this.pointerStarted = false;
        return;
      }
      this.pointerStarted = true;
      this.capturePointer(event);
    },
    handlePointerEnd(event: PointerEvent) {
      if (!this.pointerStarted || !this.isPrimaryPointer(event)) {
        this.pointerStarted = false;
        return;
      }
      this.pointerStarted = false;
      this.releasePointer(event);
      this.lastPointerActivateAt = Date.now();
      this.$emit('activate');
    },
    handlePointerCancel() {
      this.pointerStarted = false;
    },
    capturePointer(event: PointerEvent) {
      const target = event.currentTarget;
      if (!(target instanceof HTMLElement)) {
        return;
      }
      try {
        target.setPointerCapture(event.pointerId);
      } catch (error) {
        console.warn('Failed to capture action pointer:', error);
      }
    },
    releasePointer(event: PointerEvent) {
      const target = event.currentTarget;
      if (!(target instanceof HTMLElement)) {
        return;
      }
      try {
        target.releasePointerCapture(event.pointerId);
      } catch (error) {
        console.warn('Failed to release action pointer:', error);
      }
    },
    handleClick(event: MouseEvent) {
      if (!this.isPrimaryClick(event)) {
        return;
      }
      if (Date.now() - this.lastPointerActivateAt < CLICK_SUPPRESSION_MS) {
        return;
      }
      this.$emit('activate');
    }
  }
};
</script>
