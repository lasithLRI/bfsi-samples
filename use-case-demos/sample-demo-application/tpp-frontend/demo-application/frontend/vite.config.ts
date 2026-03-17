import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/ob-demo-backend-1.0.0/',
  server: {
    proxy: {
      '/ob-demo-backend-1.0.0/init': 'http://localhost:8080',
    }
  }
})
