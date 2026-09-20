import { Navigate, Route, Routes } from "react-router-dom";
import { DashboardPage } from "./pages/DashboardPage";

/**
 * 商家后台路由壳
 *
 * 入口：main.tsx → App
 * 规划生产路径：/seller（本地端口 5174）
 * 本轮仅仪表盘占位；装修/商品/履约在 I1+。
 */
export function App() {
  return (
    <Routes>
      <Route path="/" element={<DashboardPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}