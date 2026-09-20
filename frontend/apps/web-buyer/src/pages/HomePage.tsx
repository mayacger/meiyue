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

interface PlatformCoupon {
  id: number;
  code: string;
  title: string;
  discountCents: number;
  minSpendCents: number;
}

interface Claim {
  id: number;
  couponId: number;
  status: string;
}

interface NotificationItem {
  id: number;
  title: string;
  body: string;
  category: string;
  read: boolean;
}

/** 买家：登录 / 搜索 / 购物车 / 下单（店券与平台券互斥）/ 模拟支付 / 站内通知 */
export function HomePage() {
  const [products, setProducts] = useState<Array<{ id: number; tenantId: number; title: string; skus: { id: number; priceCents: number }[] }>>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [platformCoupons, setPlatformCoupons] = useState<PlatformCoupon[]>([]);
  const [platformClaims, setPlatformClaims] = useState<Claim[]>([]);
  const [storeClaims, setStoreClaims] = useState<Claim[]>([]);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const [q, setQ] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [selectedStoreClaimId, setSelectedStoreClaimId] = useState<string>("");
  const [selectedPlatformClaimId, setSelectedPlatformClaimId] = useState<string>("");
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const loggedIn = !!getToken();

  async function reloadPublic(keyword = q, cat = categoryId) {
    const params = new URLSearchParams();
    if (keyword.trim()) params.set("q", keyword.trim());
    if (cat.trim()) params.set("categoryId", cat.trim());
    const qs = params.toString();
    const res = await fetch(`/api/v1/products${qs ? `?${qs}` : ""}`);
    const body = await res.json();
    if (body.success) setProducts(body.data);
    const coupons = await fetch("/api/v1/platform-coupons").then((r) => r.json());
    if (coupons.success) setPlatformCoupons(coupons.data);
  }

  async function reloadPrivate() {
    if (!getToken()) return;
    setCart(await apiFetch<CartItem[]>("/api/v1/buyer/cart"));
    setOrders(await apiFetch<Order[]>("/api/v1/buyer/orders"));
    setPlatformClaims(await apiFetch<Claim[]>("/api/v1/buyer/platform-coupons/claims"));
    setStoreClaims(await apiFetch<Claim[]>("/api/v1/buyer/coupons/claims"));
    setNotifications(await apiFetch<NotificationItem[]>("/api/v1/notifications"));
    const uc = await apiFetch<{ unread: number }>("/api/v1/notifications/unread-count");
    setUnread(uc.unread);
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

  async function claimPlatform(couponId: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/platform-coupons/${couponId}/claim`, { method: "POST" });
      setMsg("已领取平台券");
      await reloadPrivate();
    } catch (err) {
      setError(err instanceof Error ? err.message : "领券失败");
    }
  }

  async function checkout() {
    setError(null);
    // 互斥：不可同时传店券与平台券 claim
    if (selectedStoreClaimId && selectedPlatformClaimId) {
      setError("店券与平台券不可同时使用（MUTUAL_EXCLUSIVE）");
      return;
    }
    try {
      const body: Record<string, number> = {};
      if (selectedStoreClaimId) body.storeCouponClaimId = Number(selectedStoreClaimId);
      if (selectedPlatformClaimId) body.platformCouponClaimId = Number(selectedPlatformClaimId);
      const order = await apiFetch<Order>("/api/v1/buyer/orders/checkout", { method: "POST", json: body });
      setMsg(`下单成功 ${order.orderNo}，待支付`);
      setSelectedStoreClaimId("");
      setSelectedPlatformClaimId("");
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

  async function markRead(id: number) {
    await apiFetch(`/api/v1/notifications/${id}/read`, { method: "POST" });
    await reloadPrivate();
  }

  return (
    <PageShell title="买家商城" subtitle="搜索 · 平台券 · 评价 · 通知（I3–I10）">
      {!loggedIn ? (
        <form onSubmit={login} style={{ display: "grid", gap: 8, maxWidth: 320 }}>
          <label>用户名 <input value={username} onChange={(e) => setUsername(e.target.value)} /></label>
          <label>密码 <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
          <button type="submit">登录</button>
        </form>
      ) : (
        <p>
          已登录{" "}
          <button type="button" onClick={() => { setToken(null); setCart([]); setOrders([]); setNotifications([]); }}>退出</button>
          {" · "}未读通知 {unread}
        </p>
      )}
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p>{msg}</p> : null}

      <h2>搜索</h2>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          reloadPublic().catch((err) => setError(err instanceof Error ? err.message : "搜索失败"));
        }}
        style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12 }}
      >
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="标题/类目关键词" />
        <input value={categoryId} onChange={(e) => setCategoryId(e.target.value)} placeholder="类目ID（可选）" />
        <button type="submit">搜索</button>
        <button type="button" onClick={() => { setQ(""); setCategoryId(""); reloadPublic("", "").catch(() => undefined); }}>清空</button>
      </form>

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

      <h2>平台券</h2>
      <ul>
        {platformCoupons.map((c) => (
          <li key={c.id}>
            {c.code} · {c.title} · 减¥{(c.discountCents / 100).toFixed(2)} · 满¥{(c.minSpendCents / 100).toFixed(2)}{" "}
            {loggedIn ? <button type="button" onClick={() => claimPlatform(c.id)}>领取</button> : null}
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
          <p style={{ fontSize: 13, color: "#555" }}>券规则：店券与平台券互斥，不可同时选择</p>
          <label>
            店券 claim{" "}
            <select value={selectedStoreClaimId} onChange={(e) => { setSelectedStoreClaimId(e.target.value); if (e.target.value) setSelectedPlatformClaimId(""); }}>
              <option value="">不使用</option>
              {storeClaims.filter((c) => c.status === "CLAIMED").map((c) => (
                <option key={c.id} value={c.id}>#{c.id} coupon={c.couponId}</option>
              ))}
            </select>
          </label>{" "}
          <label>
            平台券 claim{" "}
            <select value={selectedPlatformClaimId} onChange={(e) => { setSelectedPlatformClaimId(e.target.value); if (e.target.value) setSelectedStoreClaimId(""); }}>
              <option value="">不使用</option>
              {platformClaims.filter((c) => c.status === "CLAIMED").map((c) => (
                <option key={c.id} value={c.id}>#{c.id} coupon={c.couponId}</option>
              ))}
            </select>
          </label>{" "}
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

          <h2>站内通知</h2>
          <ul>
            {notifications.map((n) => (
              <li key={n.id}>
                [{n.category}] {n.title} — {n.body} {n.read ? "（已读）" : (
                  <button type="button" onClick={() => markRead(n.id)}>标已读</button>
                )}
              </li>
            ))}
          </ul>
        </>
      ) : null}
    </PageShell>
  );
}
