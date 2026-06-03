import { defineConfig } from 'vite';

export default defineConfig({
  root: '.',
  server: {
    port: 5184,
    proxy: {
      '/api': 'http://localhost:5185',
      '/asset': 'http://localhost:5185'
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        workspace: 'workspace.html',
        manipulations: 'manipulations.html'
      }
    }
  }
});
