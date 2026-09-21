import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { EmptyState, ErrorState } from "@meiyue/ui";
import type { ProductSummary, StoreInfo } from "@meiyue/types";
import "./StorePage.css";

/**
 * 买家店铺页（I22）
 * 入口：/stores/:tenantId 或商品详情「进店」
 * API：GET /stores/{tenantId} · GET /stores/{tenantId}/products
 *
 * 关系：Seller PUT /seller/store → 本页展示 name/description/logoUrl
 */
export function StorePage() {
  const { tenantId } = useParams();
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!tenantId) return;
    setLoading(true);
    setError(null);
    Promise.all([
      fetch(`/api/v1/stores/${tenantId}`).then((r) => r.json()),
      fetch(`/api/v1/stores/${tenantId}/products`).then((r) => r.json())
    ])
      .then(([s, p]) => {
        if (!s.success) throw new Error(s.message || "店铺不存在");
        setStore(s.data);
        setProducts(p.success ? p.data : []);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [tenantId]);

  return (
    <div className="my-store my-page">
      {loading ? (
        <div className="my-state my-fade-up" aria-live="polite">
          <span className="my-state__pulse" />
          正在打开店铺…
        </div>
      ) : null}
      {error ? (
        <ErrorState message={error}>
          <Link to="/products">返回商品列表</Link>
        </ErrorState>
      ) : null}
      {!loading && store ? (
        <>
          <header className="my-store__hero my-fade-up">
            {store.logoUrl ? (
              <img className="my-store__logo" src={store.logoUrl} alt="" />
            ) : (
              <span className="my-store__mark">{store.name.slice(0, 1)}</span>
            )}
            <div>
              <p className="my-store__eyebrow">美月商城 · 店铺</p>
              <h1>{store.name}</h1>
              <p className="my-store__desc">{store.description || "这家店还没有填写简介"}</p>
              <p className="my-muted">slug · {store.slug}</p>
            </div>
          </header>
          <section className="my-store__goods my-fade-up-delay">
            <h2>在售商品</h2>
            {products.length === 0 ? (
              <EmptyState title="暂无在售商品" hint="卖家上架后会出现在这里">
                <Link to="/products">逛逛全站</Link>
              </EmptyState>
            ) : (
              <ul className="my-store__grid">
                {products.map((p) => (
                  <li key={p.id}>
                    <Link to={`/products/${p.id}`}>
                      <span className="my-store__thumb">{p.title.slice(0, 1)}</span>
                      <strong>{p.title}</strong>
                      <em>¥{((p.skus[0]?.priceCents ?? 0) / 100).toFixed(2)}</em>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      ) : null}
    </div>
  );
}
