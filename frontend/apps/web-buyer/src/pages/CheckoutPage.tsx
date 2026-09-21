import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { CartItem, CouponClaim, FreightEstimate, OrderSummary } from "@meiyue/types";
import "./CheckoutPage.css";

/**
 * 结算页（I17 + I31）
 * POST /buyer/orders/freight-estimate · /buyer/orders/checkout
 * 运费：按店默认运费 / 包邮门槛；券后商品 + 运费 = 应付
 */
export function CheckoutPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<CartItem[]>([]);
  const [storeClaims, setStoreClaims] = useState<CouponClaim[]>([]);
  const [platformClaims, setPlatformClaims] = useState<CouponClaim[]>([]);
  const [storeClaimId, setStoreClaimId] = useState("");
  const [platformClaimId, setPlatformClaimId] = useState("");
  const [freight, setFreight] = useState<FreightEstimate | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    (async () => {
      try {
        const c = await apiFetch<CartItem[]>("/api/v1/buyer/cart");
        setCart(c);
        setStoreClaims(await apiFetch<CouponClaim[]>("/api/v1/buyer/coupons/claims"));
        setPlatformClaims(await apiFetch<CouponClaim[]>("/api/v1/buyer/platform-coupons/claims"));
        if (c.length > 0) {
          setFreight(
            await apiFetch<FreightEstimate>("/api/v1/buyer/orders/freight-estimate", {
              method: "POST",
              json: {}
            })
          );
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : "加载失败");
      }
    })();
  }, [navigate]);

  async function checkout() {
    setError(null);
    if (storeClaimId && platformClaimId) {
      setError("店券与平台券不可同时使用（MUTUAL_EXCLUSIVE）");
      return;
    }
    try {
      const body: Record<string, number> = {};
      if (storeClaimId) body.storeCouponClaimId = Number(storeClaimId);
      if (platformClaimId) body.platformCouponClaimId = Number(platformClaimId);
      const order = await apiFetch<OrderSummary>("/api/v1/buyer/orders/checkout", {
        method: "POST",
        json: body
      });
      setMsg(`下单成功 ${order.orderNo}`);
      navigate(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "下单失败");
    }
  }

  const goods = cart.reduce((s, c) => s + c.lineTotalCents, 0);
  const freightCents = freight?.freightCents ?? 0;

  return (
    <div className="my-checkout my-page">
      <h1 className="my-page-title my-fade-up">结算</h1>
      <p className="my-page-lead">
        共 {cart.length} 件 · 商品 ¥{(goods / 100).toFixed(2)}
        {freight ? ` · 运费 ¥${(freightCents / 100).toFixed(2)}` : ""} ·{" "}
        <Link to="/cart">返回购物车</Link>
      </p>
      {cart.length === 0 ? (
        <div className="my-empty">
          购物车为空，<Link to="/products">先去选购</Link>
        </div>
      ) : (
        <div className="my-checkout__panel my-fade-up">
          {freight ? (
            <div className="my-checkout__freight">
              <p>
                <strong>运费预估</strong> · 商品 ¥{(freight.goodsCents / 100).toFixed(2)} + 运费 ¥
                {(freight.freightCents / 100).toFixed(2)} = ¥{(freight.totalCents / 100).toFixed(2)}
                （未扣券）
              </p>
              <ul>
                {freight.shops.map((s) => (
                  <li key={s.tenantId}>
                    {s.storeName}：运费 ¥{(s.freightCents / 100).toFixed(2)}
                    {s.freeShipping
                      ? "（已包邮）"
                      : s.thresholdCents != null
                        ? `（满 ¥${(s.thresholdCents / 100).toFixed(0)} 包邮）`
                        : ""}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
          <p className="my-hint">券规则：店券与平台券互斥，不可同时选择</p>
          <label>
            店券
            <select
              value={storeClaimId}
              onChange={(e) => {
                setStoreClaimId(e.target.value);
                if (e.target.value) setPlatformClaimId("");
              }}
            >
              <option value="">不使用</option>
              {storeClaims
                .filter((c) => c.status === "CLAIMED")
                .map((c) => (
                  <option key={c.id} value={c.id}>
                    claim #{c.id} · coupon {c.couponId}
                  </option>
                ))}
            </select>
          </label>
          <label>
            平台券
            <select
              value={platformClaimId}
              onChange={(e) => {
                setPlatformClaimId(e.target.value);
                if (e.target.value) setStoreClaimId("");
              }}
            >
              <option value="">不使用</option>
              {platformClaims
                .filter((c) => c.status === "CLAIMED")
                .map((c) => (
                  <option key={c.id} value={c.id}>
                    claim #{c.id} · coupon {c.couponId}
                  </option>
                ))}
            </select>
          </label>
          {error ? <p className="my-error">{error}</p> : null}
          {msg ? <p className="my-ok">{msg}</p> : null}
          <button type="button" className="my-btn my-btn--primary" onClick={checkout}>
            确认下单
          </button>
        </div>
      )}
    </div>
  );
}
