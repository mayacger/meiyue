import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/** 买家 PC：5173，代理 API */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: true }
    }
  }
});
