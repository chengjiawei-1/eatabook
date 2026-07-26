import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import tsconfigPaths from "vite-tsconfig-paths";
import path from "node:path";

declare module "d3" {
  interface SimulationNodeDatum {
    id?: string;
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const backendTarget = env.VITE_DEV_SERVER_PROXY_TARGET || "http://localhost:8081";
  const qweatherTarget = env.VITE_QWEATHER_API_HOST || "https://q46hexjuqx.re.qweatherapi.com";

  return {
    plugins: [vue(), tsconfigPaths()],
    base: "./",
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
        vue: "vue/dist/vue.esm-bundler.js",
      },
    },
    server: {
      host: env.VITE_DEV_SERVER_HOST || "localhost",
      port: Number(env.VITE_DEV_SERVER_PORT || 3001),
      open: true,
      proxy: {
        "/api": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/uploads": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/weather-api": {
          target: qweatherTarget,
          changeOrigin: true,
          rewrite: (proxyPath) => proxyPath.replace(/^\/weather-api/, ""),
        },
      },
    },
  };
});
