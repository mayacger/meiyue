import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/**
 * 平台后台 Vite
 * - 端口 5175；代理 /api → meiyue-boot:8080
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: true }
    }
  }
});
