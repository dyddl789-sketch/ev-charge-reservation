import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8383",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
      "/images": {
        target: "http://localhost:8383",
        changeOrigin: true,
      },
      "/upload": {
        target: "http://localhost:8383",
        changeOrigin: true,
      },
      "/oauth2": {
        target: "http://localhost:8383",
        changeOrigin: true,
      },
    },
  },
});