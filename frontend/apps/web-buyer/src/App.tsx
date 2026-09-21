import { Navigate, Route, Routes } from "react-router-dom";
import { SiteShell } from "./components/SiteShell";
import { HomePage } from "./pages/HomePage";
import { ProductListPage } from "./pages/ProductListPage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { CartPage } from "./pages/CartPage";
import { CheckoutPage } from "./pages/CheckoutPage";
import { OrdersPage } from "./pages/OrdersPage";
import { OrderDetailPage } from "./pages/OrderDetailPage";
import { LoginPage } from "./pages/LoginPage";
import { AftersalesPage } from "./pages/AftersalesPage";
import { AddressPage } from "./pages/AddressPage";

/**
 * 买家 PC 路由
 * / 首页英雄 · /products 列表 · /products/:id 详情
 * /cart /checkout · /orders /orders/:id · /addresses · /aftersales · /login
 */
export function App() {
  return (
    <Routes>
      <Route element={<SiteShell />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/addresses" element={<AddressPage />} />
        <Route path="/aftersales" element={<AftersalesPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
