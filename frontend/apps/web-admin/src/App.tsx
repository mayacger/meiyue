import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "@meiyue/api";
import { AdminLayout } from "./layouts/AdminLayout";
import { LoginPage } from "./pages/LoginPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { CouponsPage } from "./pages/CouponsPage";
import { NotificationsPage } from "./pages/NotificationsPage";

/** 未登录跳转登录页 */
function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

/**
 * 平台后台路由
 * /login 登录
 * /onboarding 入驻审核
 * /coupons 平台券
 * /notifications 站内通知
 */
export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <AdminLayout />
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to="/onboarding" replace />} />
        <Route path="onboarding" element={<OnboardingPage />} />
        <Route path="coupons" element={<CouponsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
