import { Navigate, Route, Routes } from "react-router-dom";
import { HomePage } from "./pages/HomePage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { StorePage } from "./pages/StorePage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/products/:id" element={<ProductDetailPage />} />
      <Route path="/stores/:tenantId" element={<StorePage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
