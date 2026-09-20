import { Navigate, Route, Routes } from "react-router-dom";
import { OverviewPage } from "./pages/OverviewPage";

/**
 * 平台后台路由壳
 *
 * 规划生产路径：/admin（本地端口 5175）
 * 本轮仅总览占位。
 */
export function App() {
  return (
    <Routes>
      <Route path="/" element={<OverviewPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
