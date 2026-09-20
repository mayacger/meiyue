import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/**
 * 买家商城 Vite 配置
 * - port：5173
 * - 规划入口路径意向：/（本地开发各自独立端口，生产由网关路由）
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // 本地联调 API：转发到 meiyue-boot
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
});
