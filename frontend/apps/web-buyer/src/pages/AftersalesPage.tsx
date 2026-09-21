import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { EmptyState, Skeleton } from "@meiyue/ui";
import { SeoHead } from "../components/SeoHead";
import "./AftersalesPage.css";

/** 常用承运商（退货物流填写） */
const CARRIERS = [
  { code: "SF", label: "顺丰速运" },
  { code: "YTO", label: "圆通速递" },
  { code: "ZTO", label: "中通快递" },
  { code: "STO", label: "申通快递" },
  { code: "YD", label: "韵达快递" },
  { code: "JT", label: "极兔速递" }
];

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  refundCents: number;
  reason?: string;
  reverseShipmentId: number | null;
  evidenceImageUrls?: string[];
}

/**
 * 买家售后列表 + 退货物流填写（I17 + I29）
 *
 * 入口：/aftersales
 * API：GET /buyer/aftersales · POST /buyer/aftersales/{id}/reverse-tracking
 * 凭证图：申请时写入 evidenceImageUrls，本页只读展示
 */
export function AftersalesPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<Aftersale[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  /** 正在填写退货运单的售后 ID */
  const [fillingId, setFillingId] = useState<number | null>(null);
  const [carrierCode, setCarrierCode] = useState("SF");
  const [trackingNo, setTrackingNo] = useState("");

  async function reload() {
    setList(await apiFetch<Aftersale[]>("/api/v1/buyer/aftersales"));
  }

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    setLoading(true);
    reload()
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [navigate]);

  async function submitReverse(e: FormEvent, id: number) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      await apiFetch(`/api/v1/buyer/aftersales/${id}/reverse-tracking`, {
        method: "POST",
        json: { carrierCode, trackingNo: trackingNo.trim(), remark: "买家寄回" }
      });
      setMsg("退货运单已提交，请等待商家签收");
      setFillingId(null);
      setTrackingNo("");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "提交失败");
    }
  }

  return (
    <div className="my-as my-page">
      <SeoHead title="我的售后" description="查看售后进度并填写退货物流。" path="/aftersales" />
      <h1 className="my-page-title my-fade-up">我的售后</h1>
      <p className="my-page-lead">
        从订单详情可发起售后 · <Link to="/orders">返回订单</Link>
      </p>
      {error ? <p className="my-error">{error}</p> : null}
      {msg ? <p className="my-ok">{msg}</p> : null}
      {loading ? <Skeleton rows={4} /> : null}
      {!loading && list.length === 0 ? (
        <EmptyState title="暂无售后单" hint="确认收货后可在订单详情申请">
          <Link to="/orders">去订单</Link>
        </EmptyState>
      ) : null}
      {!loading && list.length > 0 ? (
        <ul className="my-as__list my-fade-up">
          {list.map((a) => (
            <li key={a.id} className="my-as__card">
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
              {a.reason ? <p className="my-muted">原因：{a.reason}</p> : null}
              {a.evidenceImageUrls && a.evidenceImageUrls.length > 0 ? (
                <div className="my-as__evidence">
                  {a.evidenceImageUrls.map((url) => (
                    <a key={url} href={url} target="_blank" rel="noreferrer">
                      <img src={url} alt="凭证" />
                    </a>
                  ))}
                </div>
              ) : null}
              {a.type === "RETURN_REFUND" && a.status === "APPROVED" && !a.reverseShipmentId ? (
                <div className="my-as__reverse">
                  {fillingId === a.id ? (
                    <form onSubmit={(e) => submitReverse(e, a.id)} className="my-as__form">
                      <label>
                        承运商
                        <select value={carrierCode} onChange={(e) => setCarrierCode(e.target.value)}>
                          {CARRIERS.map((c) => (
                            <option key={c.code} value={c.code}>
                              {c.label}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        运单号
                        <input
                          value={trackingNo}
                          onChange={(e) => setTrackingNo(e.target.value)}
                          placeholder="填写快递单号"
                          required
                        />
                      </label>
                      <div className="my-as__form-actions">
                        <button type="submit" className="my-btn my-btn--primary">
                          提交运单
                        </button>
                        <button
                          type="button"
                          className="my-btn my-btn--ghost"
                          onClick={() => setFillingId(null)}
                        >
                          取消
                        </button>
                      </div>
                    </form>
                  ) : (
                    <button
                      type="button"
                      className="my-btn my-btn--primary"
                      onClick={() => {
                        setFillingId(a.id);
                        setCarrierCode("SF");
                        setTrackingNo("");
                      }}
                    >
                      填写退货物流
                    </button>
                  )}
                </div>
              ) : null}
              {a.reverseShipmentId ? (
                <p className="my-muted">已填退货运单 · 运单 ID #{a.reverseShipmentId}</p>
              ) : null}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
