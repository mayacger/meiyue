import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { OrderSummary } from "@meiyue/types";
import "./OrdersPage.css";

/** 订单列表（I17 空态统一） */
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
    <div className="my-orders my-page">
      <h1 className="my-page-title my-fade-up">我的订单</h1>
      <p className="my-page-lead">
        待支付可模拟支付 · <Link to="/aftersales">售后列表</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {orders.length === 0 ? (
        <div className="my-empty">
          暂无订单，<Link to="/products">去逛逛</Link>
        </div>
      ) : (
        <ul className="my-fade-up">
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
      )}
    </div>
  );
}
