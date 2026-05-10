<template>
  <div class="relative overflow-hidden">
    <!-- メインコンテンツ（スワイプ可能） -->
    <div
      :style="{ transform: `translateX(${swipeOffset}px)` }"
      class="transition-transform duration-200 ease-out"
      @pointerdown="handlePointerStart"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerEnd"
      @pointercancel="handlePointerEnd"
      style="touch-action: pan-y; user-select: none"
    >
      <slot />
    </div>

    <!-- 隠しアクション（右側に隠れている） -->
    <div
      v-if="$slots.hiddenActions"
      class="absolute right-0 top-0 h-full flex items-center justify-center transition-[width,opacity] duration-200 ease-out"
      :style="{
        width: Math.abs(swipeOffset) + 'px',
        opacity: showHiddenActions ? 1 : 0,
        backgroundColor: hiddenBgColor
      }"
    >
      <div v-if="showHiddenActions" class="px-2">
        <slot name="hiddenActions" />
      </div>
    </div>
  </div>
</template>

<script>
const SWIPE_START_THRESHOLD = 8;

export default {
  name: 'SwipeContainer',
  props: {
    // スワイプしきい値（px）
    threshold: {
      type: Number,
      default: 60
    },
    // 最大スワイプ距離（px）
    maxSwipe: {
      type: Number,
      default: 100
    },
    // 隠しエリアの背景色
    hiddenBgColor: {
      type: String,
      default: '#fef7f0' // wood-50 相当
    }
  },
  emits: ['swipeStateChange'],
  data() {
    return {
      swipeOffset: 0,
      isDragging: false,
      isSwiping: false,
      startX: 0,
      startY: 0,
      currentX: 0,
      activePointerId: null
    };
  },
  computed: {
    showHiddenActions() {
      return this.swipeOffset < -this.threshold;
    }
  },
  mounted() {
    // 外部クリックで閉じる
    document.addEventListener('click', this.handleOutsideClick);
  },
  unmounted() {
    document.removeEventListener('click', this.handleOutsideClick);
  },
  watch: {
    showHiddenActions(newValue) {
      // 親コンポーネントにスワイプ状態を通知
      this.$emit('swipeStateChange', newValue);
    }
  },
  methods: {
    // 統一されたポインターイベント（マウス・タッチ・ペン全対応）
    handlePointerStart(e) {
      if (this.isDragging || this.activePointerId !== null) {
        return;
      }
      this.activePointerId = e.pointerId;
      this.startDrag(e.clientX, e.clientY);
    },
    handlePointerMove(e) {
      if (!this.isDragging || e.pointerId !== this.activePointerId) {
        return;
      }

      const diffX = e.clientX - this.startX;
      const diffY = e.clientY - this.startY;

      if (!this.isSwiping) {
        if (Math.abs(diffX) < SWIPE_START_THRESHOLD) {
          return;
        }
        if (Math.abs(diffY) > Math.abs(diffX) || diffX > 0) {
          return;
        }
        this.isSwiping = true;
        this.capturePointer(e);
      }

      e.preventDefault();
      this.updateDrag(e.clientX);
    },
    handlePointerEnd(e) {
      if (e.pointerId !== this.activePointerId) {
        return;
      }
      this.releasePointer(e);
      this.endDrag();
    },
    capturePointer(e) {
      if (this.activePointerId === null || !e.currentTarget?.setPointerCapture) {
        return;
      }
      try {
        e.currentTarget.setPointerCapture(this.activePointerId);
      } catch (error) {
        // ポインターキャプチャに失敗してもドラッグフローは継続
        console.warn('Failed to capture pointer:', error);
      }
    },
    releasePointer(e) {
      if (this.activePointerId === null || !e.currentTarget?.releasePointerCapture) {
        return;
      }
      try {
        e.currentTarget.releasePointerCapture(this.activePointerId);
      } catch (error) {
        // ポインターキャプチャの解放に失敗しても後続処理は継続
        console.warn('Failed to release pointer capture:', error);
      } finally {
        this.activePointerId = null;
      }
    },

    // 共通のドラッグロジック
    startDrag(clientX, clientY) {
      this.isDragging = true;
      this.isSwiping = false;
      this.startX = clientX;
      this.startY = clientY;
      this.currentX = clientX;
    },
    updateDrag(clientX) {
      this.currentX = clientX;
      const diff = this.currentX - this.startX;

      // 左スワイプ（負の値）のみ許可
      if (diff <= 0) {
        this.swipeOffset = Math.max(diff, -this.maxSwipe);
      }
    },
    endDrag() {
      this.isDragging = false;
      this.isSwiping = false;
      this.activePointerId = null;

      // しきい値を超えていない場合は元に戻す
      if (this.swipeOffset > -this.threshold) {
        this.resetSwipe();
      }
    },
    resetSwipe() {
      this.swipeOffset = 0;
    },
    handleOutsideClick(e) {
      // このコンポーネント外をクリックした時にスワイプを閉じる
      if (!this.$el?.contains(e.target)) {
        this.resetSwipe();
      }
    }
  }
};
</script>
