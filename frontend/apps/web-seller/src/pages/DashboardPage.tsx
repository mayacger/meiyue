import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { StoreInfo, UserProfile } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

export function DashboardPage() {
  const nav = useNavigate();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const profile = await apiFetch<UserProfile>("/api/v1/auth/me");
        setMe(profile);
        if (!profile.tenantId) {
          nav("/onboarding");
          return;
        }
        const s = await apiFetch<StoreInfo>("/api/v1/seller/store");
        setStore(s);
      } catch (err) {
        setError(err instanceof Error ? err.message : "加载失败");
      }
    })();
  }, [nav]);

  return (
    <PageShell title="商家后台" subtitle="店铺概览（I1–I10）">
      <p>
        <button type="button" onClick={() => { setToken(null); nav("/login"); }}>退出</button>{" "}
        <Link to="/onboarding">入驻</Link> · <Link to="/products">商品</Link> · <Link to="/decoration">装修</Link>
        {" · "}<Link to="/shipments">发货</Link> · <Link to="/aftersales">售后</Link>
        {" · "}<Link to="/settlements">结算</Link> · <Link to="/coupons">店券</Link>
        {" · "}<Link to="/reviews">评价</Link>
      </p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {me ? <section><h2>当前用户</h2><pre>{JSON.stringify(me, null, 2)}</pre></section> : null}
      {store ? <section><h2>我的店铺</h2><pre>{JSON.stringify(store, null, 2)}</pre></section> : <p>加载中…</p>}
    </PageShell>
  );
}
