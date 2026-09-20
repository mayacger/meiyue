import { Navigate, Route, Routes } from "react-router-dom";
import { HomePage } from "./pages/HomePage";

/**
 * 买家商城路由壳
 *
 * 入口关系：
 *   main.tsx → App → Routes
 *     /        HomePage（首页壳）
 *     *        回首页
 *
 * 规划路径：生产网关将买家端挂在 `/`；本地独立端口 5173。
 * 本轮不做商品/购物车业务。
 */
export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
