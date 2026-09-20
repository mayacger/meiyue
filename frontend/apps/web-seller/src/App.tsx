import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { getToken } from "@meiyue/api";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { DashboardPage } from "./pages/DashboardPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { ProductsPage } from "./pages/ProductsPage";
import { DecorationPage } from "./pages/DecorationPage";
import { ShipmentsPage } from "./pages/ShipmentsPage";
import { AftersalesPage } from "./pages/AftersalesPage";
import { SettlementsPage } from "./pages/SettlementsPage";
import { CouponsPage } from "./pages/CouponsPage";
import { ReviewsPage } from "./pages/ReviewsPage";

function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/onboarding" element={<RequireAuth><OnboardingPage /></RequireAuth>} />
      <Route path="/products" element={<RequireAuth><ProductsPage /></RequireAuth>} />
      <Route path="/decoration" element={<RequireAuth><DecorationPage /></RequireAuth>} />
      <Route path="/shipments" element={<RequireAuth><ShipmentsPage /></RequireAuth>} />
      <Route path="/aftersales" element={<RequireAuth><AftersalesPage /></RequireAuth>} />
      <Route path="/settlements" element={<RequireAuth><SettlementsPage /></RequireAuth>} />
      <Route path="/coupons" element={<RequireAuth><CouponsPage /></RequireAuth>} />
      <Route path="/reviews" element={<RequireAuth><ReviewsPage /></RequireAuth>} />
      <Route path="/" element={<RequireAuth><DashboardPage /></RequireAuth>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
