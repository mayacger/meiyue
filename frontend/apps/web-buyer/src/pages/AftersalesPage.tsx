import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import "./AftersalesPage.css";

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  refundCents: number;
  reason?: string;
}

/**
 * 买家售后列表（I17 缺页补齐）
 * GET /buyer/aftersales
 */
export function AftersalesPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<Aftersale[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    apiFetch<Aftersale[]>("/api/v1/buyer/aftersales")
      .then(setList)
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [navigate]);

  return (
    <div className="my-as my-page">
      <h1 className="my-page-title my-fade-up">我的售后</h1>
      <p className="my-page-lead">
        从订单详情可发起售后 · <Link to="/orders">返回订单</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {list.length === 0 ? (
        <div className="my-empty">暂无售后单</div>
      ) : (
        <ul className="my-as__list my-fade-up">
          {list.map((a) => (
            <li key={a.id}>
              <div>
                <strong>{a.aftersaleNo}</strong>
                <span className="my-muted">
                  {" "}
                  · 订单 <Link to={`/orders/${a.orderId}`}>#{a.orderId}</Link>
                </span>
              </div>
              <div>
                {a.type} · {a.status} · ¥{(a.refundCents / 100).toFixed(2)}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
