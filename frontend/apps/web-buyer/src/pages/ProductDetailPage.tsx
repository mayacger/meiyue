import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { ProductSummary } from "@meiyue/types";
import "./ProductDetailPage.css";

interface Review {
  id: number;
  rating: number;
  content: string;
  sellerReply: string | null;
}

/**
 * 商品详情
 * GET /products/:id · /products/:id/reviews · POST 加购
 */
export function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      fetch(`/api/v1/products/${id}`).then((r) => r.json()),
      fetch(`/api/v1/products/${id}/reviews`).then((r) => r.json())
    ])
      .then(([prod, rev]) => {
        if (!prod.success) throw new Error(prod.message);
        setProduct(prod.data);
        if (rev.success) setReviews(rev.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, [id]);

  async function addToCart() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    const skuId = product?.skus[0]?.id;
    if (!skuId) return;
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/cart/items", {
        method: "POST",
        json: { skuId, quantity: 1 }
      });
      setMsg("已加入购物车");
    } catch (err) {
      setError(err instanceof Error ? err.message : "加购失败");
    }
  }

  const price = ((product?.skus[0]?.priceCents ?? 0) / 100).toFixed(2);

  return (
    <article className="my-detail">
      <p className="my-detail__crumb">
        <Link to="/products">全部商品</Link> / 详情
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}
      {product ? (
        <div className="my-detail__grid my-fade-up">
          <div className="my-detail__visual" aria-hidden>
            <span>{product.title.slice(0, 1)}</span>
          </div>
          <div className="my-detail__info">
            <h1>{product.title}</h1>
            <p className="my-detail__price">¥{price}</p>
            <p className="my-detail__sub">{product.subtitle || "精选好物 · 美月履约"}</p>
            <button type="button" className="my-btn my-btn--primary" onClick={addToCart}>
              加入购物车
            </button>
            <Link to="/cart" className="my-detail__cart-link">
              查看购物车
            </Link>
          </div>
        </div>
      ) : null}
      <section className="my-detail__reviews">
        <h2>买家评价{reviews.length > 0 ? `（${reviews.length}）` : ""}</h2>
        {reviews.length === 0 ? (
          <p className="my-muted">暂无评价，确认收货后可在订单详情提交</p>
        ) : null}
        <ul>
          {reviews.map((r) => (
            <li key={r.id}>
              <strong>★{r.rating}</strong> {r.content}
              {r.sellerReply ? <div className="my-muted">商家回复：{r.sellerReply}</div> : null}
            </li>
          ))}
        </ul>
      </section>
    </article>
  );
}
