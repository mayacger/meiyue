import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite 配置文件
// 参考文档: https://vite.dev/config/
export default defineConfig({
  // 启用 React 插件（提供快速刷新 / JSX 转换等能力）
  plugins: [react()],
  server: {
    // 监听所有网络接口，便于在 Cloud Agent 环境中通过转发端口访问
    host: true,
    // 开发服务器端口
    port: 5173,
  },
  preview: {
    // 生产构建预览服务同样监听所有接口
    host: true,
    port: 4173,
  },
})
