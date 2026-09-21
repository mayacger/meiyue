import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { CartItem } from "@meiyue/types";
import "./CartPage.css";

/** 购物车：GET /buyer/cart · DELETE 行 · 去结算（I16 视觉深化） */
export function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<CartItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setCart(await apiFetch<CartItem[]>("/api/v1/buyer/cart"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function remove(id: number) {
    await apiFetch(`/api/v1/buyer/cart/items/${id}`, { method: "DELETE" });
    await reload();
  }

  const total = cart.reduce((s, c) => s + c.lineTotalCents, 0);

  return (
    <div className="my-cart my-page">
      <h1 className="my-page-title my-fade-up">购物车</h1>
      <p className="my-page-lead">跨店合并结算 · 店券与平台券互斥</p>
      {error ? <p className="my-error">{error}</p> : null}
      {cart.length === 0 ? (
        <div className="my-empty">
          购物车为空，<Link to="/products">去逛逛</Link>
        </div>
      ) : (
        <ul className="my-cart__list my-fade-up">
          {cart.map((c) => (
            <li key={c.id}>
              <div className="my-cart__thumb" aria-hidden>
                {c.productTitle.slice(0, 1)}
              </div>
              <div className="my-cart__meta">
                <strong>{c.productTitle}</strong>
                <span className="my-muted">
                  {c.skuCode} · ×{c.quantity}
                </span>
              </div>
              <div className="my-cart__price">¥{(c.lineTotalCents / 100).toFixed(2)}</div>
              <button type="button" className="my-cart__remove" onClick={() => remove(c.id).catch(() => undefined)}>
                移除
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="my-cart__bar">
        <p>
          合计 <strong>¥{(total / 100).toFixed(2)}</strong>
        </p>
        <button
          type="button"
          className="my-btn my-btn--primary"
          disabled={cart.length === 0}
          onClick={() => navigate("/checkout")}
        >
          去结算
        </button>
      </div>
    </div>
  );
}
