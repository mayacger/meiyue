import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { ProductSummary } from "@meiyue/types";
import { ProductRail } from "../components/ProductRail";
import "./HomePage.css";

/**
 * 买家首页（I20 视觉打磨）
 * 首屏：品牌名 + 一句卖点 + CTA + 全宽英雄；加载/空态
 * API：GET /products
 */
export function HomePage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    fetch("/api/v1/products")
      .then((r) => r.json())
      .then((body) => {
        if (!body.success) throw new Error(body.message || "加载失败");
        setProducts(body.data);
        setError(null);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <section className="my-hero">
        <div className="my-hero__copy my-fade-up">
          <p className="my-hero__brand">美月商城</p>
          <h1>月色下的好店与好物</h1>
          <p className="my-hero__lead">
            多商家精选上架，完整履约与售后。清透浏览，从发现到签收一路安心。
          </p>
          <div className="my-hero__cta">
            <Link to="/products" className="my-btn my-btn--primary">
              浏览商品
            </Link>
            <Link to="/login" className="my-btn my-btn--ghost">
              登录账户
            </Link>
          </div>
        </div>
        <div className="my-hero__visual my-fade-up-delay" aria-hidden>
          <div className="my-hero__plane" />
          <div className="my-hero__orbit" />
        </div>
      </section>

      {loading ? (
        <div className="my-state my-fade-up" aria-live="polite">
          <span className="my-state__pulse" />
          正在点亮本季上架…
        </div>
      ) : null}
      {error ? <p className="my-error my-page">{error}</p> : null}
      {!loading && !error && products.length === 0 ? (
        <div className="my-empty my-fade-up">
          暂无在售商品 · <Link to="/products">稍后再逛</Link>
        </div>
      ) : null}
      {!loading && products.length > 0 ? (
        <ProductRail products={products.slice(0, 8)} title="本季上架" />
      ) : null}

      <section className="my-promise my-fade-up">
        <div className="my-promise__inner">
          <h2>完整履约，月色安心</h2>
          <p>正向物流全程可追 · 售后原路退款 · 多店一单支付 · 不做直播带货</p>
        </div>
      </section>
    </>
  );
}
