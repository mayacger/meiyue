import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { OrderSummary } from "@meiyue/types";
import "./OrdersPage.css";

/** 订单列表：GET /buyer/orders · 模拟支付 */
export function OrdersPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setOrders(await apiFetch<OrderSummary[]>("/api/v1/buyer/orders"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function mockPay(id: number) {
    await apiFetch(`/api/v1/buyer/orders/${id}/mock-pay`, { method: "POST" });
    await reload();
  }

  return (
    <div className="my-orders">
      <h1>我的订单</h1>
      {error ? <p className="my-error">{error}</p> : null}
      <ul>
        {orders.map((o) => (
          <li key={o.id}>
            <Link to={`/orders/${o.id}`}>{o.orderNo}</Link>
            <span>
              [{o.status}] ¥{(o.totalCents / 100).toFixed(2)}
            </span>
            {o.status === "PENDING_PAYMENT" ? (
              <button type="button" className="my-btn my-btn--ghost" onClick={() => mockPay(o.id)}>
                模拟支付
              </button>
            ) : null}
          </li>
        ))}
      </ul>
      {orders.length === 0 ? <p className="my-muted">暂无订单</p> : null}
    </div>
  );
}
