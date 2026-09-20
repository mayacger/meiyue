import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch, getToken, setToken } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";
import type { AuthResult } from "@meiyue/types";

interface CartItem {
  id: number;
  productTitle: string;
  skuCode: string;
  unitPriceCents: number;
  quantity: number;
  lineTotalCents: number;
  skuId: number;
}

interface Order {
  id: number;
  orderNo: string;
  status: string;
  totalCents: number;
  paymentNo?: string;
}

/** 买家：登录 / 购物车 / 下单 / 模拟支付（I3） */
export function HomePage() {
  const [products, setProducts] = useState<Array<{ id: number; tenantId: number; title: string; skus: { id: number; priceCents: number }[] }>>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const loggedIn = !!getToken();

  async function reloadPublic() {
    const res = await fetch("/api/v1/products");
    const body = await res.json();
    if (body.success) setProducts(body.data);
  }

  async function reloadPrivate() {
    if (!getToken()) return;
    setCart(await apiFetch<CartItem[]>("/api/v1/buyer/cart"));
    setOrders(await apiFetch<Order[]>("/api/v1/buyer/orders"));
  }

  useEffect(() => {
    reloadPublic().catch(() => undefined);
    reloadPrivate().catch(() => undefined);
  }, []);

  async function login(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        json: { username, password }
      });
      setToken(data.accessToken);
      setMsg("登录成功");
      await reloadPrivate();
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  }

  async function addToCart(skuId: number) {
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/cart/items", { method: "POST", json: { skuId, quantity: 1 } });
      await reloadPrivate();
      setMsg("已加入购物车");
    } catch (err) {
      setError(err instanceof Error ? err.message : "加购失败");
    }
  }

  async function checkout() {
    setError(null);
    try {
      const order = await apiFetch<Order>("/api/v1/buyer/orders/checkout", { method: "POST", json: {} });
      setMsg(`下单成功 ${order.orderNo}，待支付`);
      await reloadPrivate();
    } catch (err) {
      setError(err instanceof Error ? err.message : "下单失败");
    }
  }

  async function mockPay(id: number) {
    setError(null);
    try {
      const order = await apiFetch<Order>(`/api/v1/buyer/orders/${id}/mock-pay`, { method: "POST" });
      setMsg(`支付成功 ${order.orderNo}`);
      await reloadPrivate();
    } catch (err) {
      setError(err instanceof Error ? err.message : "支付失败");
    }
  }

  return (
    <PageShell title="买家商城" subtitle="浏览 · 加购 · 下单 · 物流售后（I3–I9）">
      {!loggedIn ? (
        <form onSubmit={login} style={{ display: "grid", gap: 8, maxWidth: 320 }}>
          <label>用户名 <input value={username} onChange={(e) => setUsername(e.target.value)} /></label>
          <label>密码 <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
          <button type="submit">登录</button>
        </form>
      ) : (
        <p>
          已登录{" "}
          <button type="button" onClick={() => { setToken(null); setCart([]); setOrders([]); }}>退出</button>
        </p>
      )}
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p>{msg}</p> : null}

      <h2>上架商品</h2>
      <ul>
        {products.map((p) => (
          <li key={p.id}>
            <Link to={`/products/${p.id}`}>{p.title}</Link> — ¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}{" "}
            <Link to={`/stores/${p.tenantId}`}>进店</Link>{" "}
            {loggedIn && p.skus[0] ? (
              <button type="button" onClick={() => addToCart(p.skus[0].id)}>加购</button>
            ) : null}
          </li>
        ))}
      </ul>

      {loggedIn ? (
        <>
          <h2>购物车</h2>
          <ul>
            {cart.map((c) => (
              <li key={c.id}>
                {c.productTitle} x{c.quantity} = ¥{(c.lineTotalCents / 100).toFixed(2)}
              </li>
            ))}
          </ul>
          <button type="button" disabled={cart.length === 0} onClick={checkout}>结算下单</button>

          <h2>我的订单</h2>
          <ul>
            {orders.map((o) => (
              <li key={o.id}>
                <Link to={`/orders/${o.id}`}>{o.orderNo}</Link> [{o.status}] ¥{(o.totalCents / 100).toFixed(2)}{" "}
                {o.status === "PENDING_PAYMENT" ? (
                  <button type="button" onClick={() => mockPay(o.id)}>模拟支付</button>
                ) : null}
              </li>
            ))}
          </ul>
        </>
      ) : null}
    </PageShell>
  );
}
