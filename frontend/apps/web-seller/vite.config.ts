import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/** 商家后台 Vite：端口 5174，代理 /api → :8080 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: true }
    }
  }
});
