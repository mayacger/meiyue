import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "@meiyue/api";
import { LoginPage } from "./pages/LoginPage";
import { PendingPage } from "./pages/PendingPage";

function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

/**
 * 平台后台路由（I1）：登录 + 待审入驻列表
 */
export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <PendingPage />
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
