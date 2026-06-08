import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

/** 全部 /api（含 /api/deepseek）由后端转发，密钥只在服务端配置 DEEPSEEK_API_KEY */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true
      }
    }
  },
  preview: {
    port: 4173,
    proxy: {
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true
      }
    }
  }
});
