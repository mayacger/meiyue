import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { EmptyState, ErrorState } from "@meiyue/ui";
import type { CouponClaim, CouponSummary } from "@meiyue/types";
import "./CouponsPage.css";

/**
 * 优惠券领取页（I23）
 * 入口：顶栏「领券」
 * API：
 *   GET /platform-coupons · POST /buyer/platform-coupons/{id}/claim
 *   GET /buyer/platform-coupons/claims · GET /buyer/coupons/claims
 * 店券需已知 tenantId；本页以平台券为主，并展示已领记录。
 */
export function CouponsPage() {
  const navigate = useNavigate();
  const [platform, setPlatform] = useState<CouponSummary[]>([]);
  const [platformClaims, setPlatformClaims] = useState<CouponClaim[]>([]);
  const [storeClaims, setStoreClaims] = useState<CouponClaim[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    setLoading(true);
    setError(null);
    try {
      const list = await fetch("/api/v1/platform-coupons").then((r) => r.json());
      if (!list.success) throw new Error(list.message);
      setPlatform(list.data);
      if (getToken()) {
        setPlatformClaims(
          await apiFetch<CouponClaim[]>("/api/v1/buyer/platform-coupons/claims")
        );
        setStoreClaims(await apiFetch<CouponClaim[]>("/api/v1/buyer/coupons/claims"));
      } else {
        setPlatformClaims([]);
        setStoreClaims([]);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, []);

  async function claim(id: number) {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setMsg(null);
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/platform-coupons/${id}/claim`, { method: "POST" });
      setMsg("领取成功，可在结算页使用");
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "领取失败");
    }
  }

  const claimedIds = new Set(platformClaims.map((c) => c.couponId));

  return (
    <div className="my-coupons my-page">
      <h1 className="my-page-title my-fade-up">领券中心</h1>
      <p className="my-page-lead">
        平台券一键领取 · 店券请到对应店铺领取 ·{" "}
        <Link to="/checkout">去结算使用</Link>
      </p>
      {msg ? <p className="my-ok">{msg}</p> : null}
      {error ? (
        <ErrorState message={error}>
          <button type="button" className="my-btn my-btn--ghost" onClick={() => reload()}>
            重试
          </button>
        </ErrorState>
      ) : null}
      {loading ? <p className="my-muted">加载中…</p> : null}
      {!loading && platform.length === 0 ? (
        <EmptyState title="暂无可领平台券" hint="平台发布后会出现在这里" />
      ) : null}
      <ul className="my-coupons__list my-fade-up">
        {platform.map((c) => {
          const claimed = claimedIds.has(c.id);
          return (
            <li key={c.id}>
              <div>
                <strong>{c.title}</strong>
                <p className="my-muted">
                  满 ¥{(c.minSpendCents / 100).toFixed(0)} 减 ¥
                  {(c.discountCents / 100).toFixed(0)} · 码 {c.code}
                </p>
              </div>
              <button
                type="button"
                className="my-btn my-btn--primary"
                disabled={claimed}
                onClick={() => claim(c.id)}
              >
                {claimed ? "已领取" : "立即领取"}
              </button>
            </li>
          );
        })}
      </ul>
      <section className="my-coupons__mine">
        <h2>我的券包</h2>
        {!getToken() ? (
          <EmptyState title="登录后查看已领券" hint="">
            <Link to="/login">去登录</Link>
          </EmptyState>
        ) : platformClaims.length + storeClaims.length === 0 ? (
          <EmptyState title="券包是空的" hint="领取上方平台券，或进店领店券" />
        ) : (
          <ul className="my-coupons__claims">
            {platformClaims.map((c) => (
              <li key={`p-${c.id}`}>
                平台券 claim #{c.id} · coupon {c.couponId} · {c.status}
              </li>
            ))}
            {storeClaims.map((c) => (
              <li key={`s-${c.id}`}>
                店券 claim #{c.id} · coupon {c.couponId} · {c.status}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
