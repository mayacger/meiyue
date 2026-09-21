import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { ProductSummary } from "@meiyue/types";
import { ProductRail } from "../components/ProductRail";
import "./HomePage.css";

/**
 * 买家首页
 * 首屏：品牌名 + 一句卖点 + CTA + 全宽英雄视觉（非卡片堆砌）
 * API：GET /products
 */
export function HomePage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);

  useEffect(() => {
    fetch("/api/v1/products")
      .then((r) => r.json())
      .then((body) => {
        if (body.success) setProducts(body.data);
      })
      .catch(() => undefined);
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
        </div>
      </section>
      <ProductRail products={products.slice(0, 8)} title="本季上架" />
    </>
  );
}
