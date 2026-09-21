import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import type { CartItem } from "@meiyue/types";
import "./CartPage.css";

/** 购物车：GET /buyer/cart · DELETE 行 · 去结算 */
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
    <div className="my-cart">
      <h1 className="my-fade-up">购物车</h1>
      {error ? <p className="my-error">{error}</p> : null}
      {cart.length === 0 ? (
        <p className="my-muted">
          购物车为空，<Link to="/products">去逛逛</Link>
        </p>
      ) : (
        <ul className="my-cart__list my-fade-up">
          {cart.map((c) => (
            <li key={c.id}>
              <div>
                <strong>{c.productTitle}</strong>
                <span className="my-muted">
                  {" "}
                  ×{c.quantity} · ¥{(c.lineTotalCents / 100).toFixed(2)}
                </span>
              </div>
              <button type="button" onClick={() => remove(c.id).catch(() => undefined)}>
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
