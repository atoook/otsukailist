import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom', // Vue コンポーネントのテストのため
    globals: true, // describe, it, expect をグローバルで使用可能
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
})