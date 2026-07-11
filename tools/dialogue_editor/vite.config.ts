import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  root: '.',
  server: {
    port: 5174,
    proxy: {
      '/api': 'http://localhost:5175'
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    chunkSizeWarningLimit: 1900,
    rollupOptions: {
      input: 'workspace.html'
    }
  }
});
