import { PropsWithChildren } from "react";
import { useLaunch } from "@tarojs/taro";
import Taro from "@tarojs/taro";
import { configureApiStorage } from "./services/api";
import "./app.css";

/**
 * Taro 应用入口
 * - 注入 Storage 适配（Taro.get/setStorageSync）
 * - 无直播能力
 */
function App({ children }: PropsWithChildren) {
  useLaunch(() => {
    configureApiStorage({
      getItem: (key) => {
        try {
          return Taro.getStorageSync(key) || null;
        } catch {
          return null;
        }
      },
      setItem: (key, value) => {
        Taro.setStorageSync(key, value);
      },
      removeItem: (key) => {
        Taro.removeStorageSync(key);
      }
    });
  });

  return children;
}

export default App;
