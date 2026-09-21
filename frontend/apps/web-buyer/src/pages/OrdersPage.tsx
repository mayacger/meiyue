import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { EmptyState, ErrorState, Skeleton } from "@meiyue/ui";
import type { OrderSummary } from "@meiyue/types";
import "./OrdersPage.css";

/** 订单列表（I23 空错态 + I29 骨架） */
export function OrdersPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      setOrders(await apiFetch<OrderSummary[]>("/api/v1/buyer/orders"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch((e) => {
      setError(e instanceof Error ? e.message : "加载失败");
      setLoading(false);
    });
  }, []);

  async function mockPay(id: number) {
    await apiFetch(`/api/v1/buyer/orders/${id}/mock-pay`, { method: "POST" });
    await reload();
  }

  async function cancelOrder(id: number) {
    await apiFetch(`/api/v1/buyer/orders/${id}/cancel`, { method: "POST" });
    await reload();
  }

  return (
    <div className="my-orders my-page">
      <h1 className="my-page-title my-fade-up">我的订单</h1>
      <p className="my-page-lead">
        待支付可模拟支付或取消 · <Link to="/aftersales">售后列表</Link>
      </p>
      {error ? (
        <ErrorState message={error}>
          <button type="button" className="my-btn my-btn--ghost" onClick={() => reload()}>
            重试
          </button>
        </ErrorState>
      ) : null}
      {loading ? <Skeleton rows={3} /> : null}
      {!loading && orders.length === 0 && !error ? (
        <EmptyState title="暂无订单" hint="去逛逛好物再回来">
          <Link to="/products">去逛逛</Link>
        </EmptyState>
      ) : null}
      {!loading && orders.length > 0 ? (
        <ul className="my-fade-up">
          {orders.map((o) => (
            <li key={o.id}>
              <Link to={`/orders/${o.id}`}>{o.orderNo}</Link>
              <span>
                [{o.status}] ¥{(o.totalCents / 100).toFixed(2)}
              </span>
              {o.status === "PENDING_PAYMENT" ? (
                <>
                  <button type="button" className="my-btn my-btn--ghost" onClick={() => mockPay(o.id)}>
                    模拟支付
                  </button>
                  <button
                    type="button"
                    className="my-btn my-btn--ghost"
                    onClick={() =>
                      cancelOrder(o.id).catch((e) =>
                        setError(e instanceof Error ? e.message : "取消失败")
                      )
                    }
                  >
                    取消订单
                  </button>
                </>
              ) : null}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
