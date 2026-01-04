<script>
/**
 * ContentArea - メインコンテンツエリアコンポーネント
 * Layout内でflex-1として縦スペースを占有し、コンテンツの配置を管理する
 *
 * @prop {String} layout - コンテンツの配置方法
 *   - 'default': 通常のコンテンツ表示（リスト、フォーム等）【現在使用中】
 *   - 'center': 完全中央配置（ローディング画面、エラー画面、初期状態表示等）
 *   - 'center-vertical': 垂直中央配置のみ（特殊なフォームレイアウト等）
 *
 * Note: 現在のプロジェクトでは 'default' のみ使用されています
 */
export default {
  name: 'ContentArea',
  props: {
    layout: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'center', 'center-vertical'].includes(value)
    }
  },
  computed: {
    containerClass() {
      const baseClass = `p-6 overflow-y-auto flex-1`;

      switch (this.layout) {
        case 'center':
          return `${baseClass} flex flex-col items-center justify-center`;
        case 'center-vertical':
          return `${baseClass} flex flex-col items-center`;
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
