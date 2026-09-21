import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "@meiyue/api";
import { SellerLayout } from "./layouts/SellerLayout";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { DashboardPage } from "./pages/DashboardPage";
import { ProductsPage } from "./pages/ProductsPage";
import { DecorationPage } from "./pages/DecorationPage";
import { ShipmentsPage } from "./pages/ShipmentsPage";
import { AftersalesPage } from "./pages/AftersalesPage";
import { SettlementsPage } from "./pages/SettlementsPage";
import { CouponsPage } from "./pages/CouponsPage";
import { AiAssistPage } from "./pages/AiAssistPage";

function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

/**
 * 商家后台路由
 * 登录/注册/入驻独立；业务页走 SellerLayout(ProLayout)
 */
export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/onboarding"
        element={
          <RequireAuth>
            <OnboardingPage />
          </RequireAuth>
        }
      />
      <Route
        path="/"
        element={
          <RequireAuth>
            <SellerLayout />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="decoration" element={<DecorationPage />} />
        <Route path="shipments" element={<ShipmentsPage />} />
        <Route path="aftersales" element={<AftersalesPage />} />
        <Route path="settlements" element={<SettlementsPage />} />
        <Route path="coupons" element={<CouponsPage />} />
        <Route path="ai" element={<AiAssistPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
