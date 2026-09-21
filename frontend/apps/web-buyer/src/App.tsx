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
import { NotificationsPage } from "./pages/NotificationsPage";
import { TicketsPage } from "./pages/TicketsPage";
import { StorePage } from "./pages/StorePage";
import { FavoritesPage } from "./pages/FavoritesPage";
import { BrowseHistoryPage } from "./pages/BrowseHistoryPage";
import { CouponsPage } from "./pages/CouponsPage";
import { SettingsPage } from "./pages/SettingsPage";
import { InvoicePage } from "./pages/InvoicePage";

/**
 * 买家 PC 路由（I24 + I30 + I33）
 * /invoices 发票抬头 · /browse-history · …
 */
export function App() {
  return (
    <Routes>
      <Route element={<SiteShell />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/stores/:tenantId" element={<StorePage />} />
        <Route path="/favorites" element={<FavoritesPage />} />
        <Route path="/browse-history" element={<BrowseHistoryPage />} />
        <Route path="/coupons" element={<CouponsPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/addresses" element={<AddressPage />} />
        <Route path="/invoices" element={<InvoicePage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="/tickets" element={<TicketsPage />} />
        <Route path="/aftersales" element={<AftersalesPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
