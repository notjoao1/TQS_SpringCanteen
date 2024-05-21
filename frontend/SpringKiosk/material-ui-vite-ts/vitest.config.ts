// vitest.config.js
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    css: true,
    setupFiles: './src/tests/setup.ts',
    environment: 'jsdom'
  },
})
