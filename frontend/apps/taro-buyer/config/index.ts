import { defineConfig, type UserConfigExport } from "@tarojs/cli";
import path from "node:path";
import devConfig from "./dev";
import prodConfig from "./prod";

/**
 * Taro 全局配置
 * - 设计：H5 + 微信小程序双端
 * - 开发代理：/api → meiyue-boot:8080
 */
export default defineConfig<"vite">(async (merge) => {
  const baseConfig: UserConfigExport<"vite"> = {
    projectName: "meiyue-taro-buyer",
    date: "2026-3-21",
    designWidth: 750,
    deviceRatio: {
      640: 2.34 / 2,
      750: 1,
      375: 2,
      828: 1.81 / 2
    },
    sourceRoot: "src",
    outputRoot: `dist/${process.env.TARO_ENV}`,
    plugins: ["@tarojs/plugin-framework-react"],
    defineConstants: {},
    copy: { patterns: [], options: {} },
    framework: "react",
    compiler: "vite",
    alias: {
      "@": path.resolve(__dirname, "..", "src")
    },
    mini: {
      /** I37：主包体积优化（自动将异步页面下沉） */
      optimizeMainPackage: {
        enable: true
      },
      postcss: {
        pxtransform: { enable: true, config: {} },
        cssModules: { enable: false }
      }
    },
    h5: {
      publicPath: "/",
      staticDirectory: "static",
      esnextModules: ["@meiyue"],
      router: { mode: "hash" },
      devServer: {
        port: 10086,
        proxy: {
          "/api": {
            target: "http://localhost:8080",
            changeOrigin: true
          }
        }
      },
      postcss: {
        autoprefixer: { enable: true },
        cssModules: { enable: false }
      }
    }
  };

  if (process.env.NODE_ENV === "development") {
    return merge({}, baseConfig, devConfig);
  }
  return merge({}, baseConfig, prodConfig);
});
