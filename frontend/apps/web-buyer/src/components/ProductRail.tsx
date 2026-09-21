import { Link } from "react-router-dom";
import type { ProductSummary } from "@meiyue/types";
import "./ProductRail.css";

function formatPrice(cents: number) {
  return `¥${(cents / 100).toFixed(2)}`;
}

/**
 * 商品横带：非卡片堆砌，大图+标题+价；用于首页与列表
 * 字段：id / title / skus[0].priceCents
 */
export function ProductRail({
  products,
  title
}: {
  products: ProductSummary[];
  title?: string;
}) {
  return (
    <section className="my-rail">
      {title ? <h2 className="my-rail__title my-fade-up">{title}</h2> : null}
      <div className="my-rail__grid">
        {products.map((p, i) => (
          <Link
            key={p.id}
            to={`/products/${p.id}`}
            className="my-rail__item my-fade-up"
            style={{ animationDelay: `${0.05 * i}s` }}
          >
            <div className="my-rail__visual" aria-hidden>
              <span>{p.title.slice(0, 1)}</span>
            </div>
            <div className="my-rail__meta">
              <h3>{p.title}</h3>
              <p>{formatPrice(p.skus[0]?.priceCents ?? 0)}</p>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
