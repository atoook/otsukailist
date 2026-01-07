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
      startX: 0,
      currentX: 0
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
      // ポインターをキャプチャ（追跡を継続）
      e.target.setPointerCapture(e.pointerId);
      this.startDrag(e.clientX);
    },
    handlePointerMove(e) {
      if (this.isDragging) {
        e.preventDefault();
        this.updateDrag(e.clientX);
      }
    },
    handlePointerEnd(e) {
      // ポインターキャプチャを解放
      e.target.releasePointerCapture(e.pointerId);
      this.endDrag();
    },

    // 共通のドラッグロジック
    startDrag(clientX) {
      this.isDragging = true;
      this.startX = clientX;
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
