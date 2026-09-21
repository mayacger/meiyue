import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "@meiyue/api";
import { AdminLayout } from "./layouts/AdminLayout";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { CouponsPage } from "./pages/CouponsPage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { CategoriesPage } from "./pages/CategoriesPage";
import { AccountPage } from "./pages/AccountPage";
import { TicketsPage } from "./pages/TicketsPage";
import { PlatformConfigPage } from "./pages/PlatformConfigPage";
import { AuditLogsPage } from "./pages/AuditLogsPage";
import { BannersPage } from "./pages/BannersPage";
import { SettlementsPage } from "./pages/SettlementsPage";
import { ReviewsPage } from "./pages/ReviewsPage";

/** 未登录跳转登录页 */
function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

/**
 * 平台后台路由（I22–I30）
 * /banners · /settlements · /reviews · /platform-config · …
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
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="onboarding" element={<OnboardingPage />} />
        <Route path="coupons" element={<CouponsPage />} />
        <Route path="banners" element={<BannersPage />} />
        <Route path="settlements" element={<SettlementsPage />} />
        <Route path="reviews" element={<ReviewsPage />} />
        <Route path="platform-config" element={<PlatformConfigPage />} />
        <Route path="audit-logs" element={<AuditLogsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="tickets" element={<TicketsPage />} />
        <Route path="categories" element={<CategoriesPage />} />
        <Route path="account" element={<AccountPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
