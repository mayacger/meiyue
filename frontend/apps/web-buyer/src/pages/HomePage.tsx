import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { ProductSummary } from "@meiyue/types";
import { Skeleton } from "@meiyue/ui";
import { ProductRail } from "../components/ProductRail";
import { SeoHead } from "../components/SeoHead";
import "./HomePage.css";

/** 平台 Banner（I28） */
interface Banner {
  id: number;
  title: string;
  imageUrl: string;
  linkUrl: string | null;
}

/**
 * 买家首页（I20 视觉 + I28 运营 Banner）
 * API：GET /banners · GET /products
 * 禁直播组件；Banner 为图文跳转运营位
 */
export function HomePage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      fetch("/api/v1/banners").then((r) => r.json()),
      fetch("/api/v1/products").then((r) => r.json())
    ])
      .then(([bBody, pBody]) => {
        if (bBody.success) setBanners(bBody.data || []);
        if (!pBody.success) throw new Error(pBody.message || "加载失败");
        setProducts(pBody.data);
        setError(null);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <SeoHead
        title="首页"
        description="美月商城 — 月色下的好店与好物。多商家精选上架，完整履约与售后。"
        path="/"
      />
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

      {banners.length > 0 ? (
        <section className="my-banners my-fade-up" aria-label="运营推荐">
          <div className="my-banners__track">
            {banners.map((b) => {
              const inner = (
                <>
                  <img src={b.imageUrl} alt={b.title} loading="lazy" />
                  <span className="my-banners__cap">{b.title}</span>
                </>
              );
              const href = b.linkUrl || "/products";
              const external = href.startsWith("http");
              return external ? (
                <a key={b.id} className="my-banners__item" href={href} rel="noreferrer">
                  {inner}
                </a>
              ) : (
                <Link key={b.id} className="my-banners__item" to={href}>
                  {inner}
                </Link>
              );
            })}
          </div>
        </section>
      ) : null}

      {loading ? (
        <div className="my-page my-fade-up">
          <Skeleton rows={4} height={18} />
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
