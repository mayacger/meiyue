import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Track {
  status: string;
  description: string;
  trackedAt: string;
}

interface Shipment {
  id: number;
  direction: string;
  status: string;
  carrierCode: string;
  trackingNo: string;
  tracks: Track[];
}

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  refundCents: number;
  reverseShipmentId: number | null;
}

/** 买家：订单物流轨迹 + 售后申请/填逆向运单 */
export function OrderDetailPage() {
  const { id } = useParams();
  const orderId = Number(id);
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [aftersales, setAftersales] = useState<Aftersale[]>([]);
  const [type, setType] = useState<"REFUND_ONLY" | "RETURN_REFUND">("REFUND_ONLY");
  const [reason, setReason] = useState("不想要了");
  const [refundYuan, setRefundYuan] = useState("99");
  const [carrier, setCarrier] = useState("SF");
  const [tracking, setTracking] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function reload() {
    if (!getToken()) return;
    setShipments(await apiFetch<Shipment[]>(`/api/v1/buyer/orders/${orderId}/shipments`));
    setAftersales(await apiFetch<Aftersale[]>("/api/v1/buyer/aftersales").then((all) => all.filter((a) => a.orderId === orderId)));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [orderId]);

  async function applyAftersale(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/buyer/aftersales", {
        method: "POST",
        json: {
          orderId,
          type,
          reason,
          refundCents: Math.round(parseFloat(refundYuan) * 100)
        }
      });
      setMsg("售后已申请");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "申请失败");
    }
  }

  async function fillReverse(aftersaleId: number) {
    setError(null);
    try {
      await apiFetch(`/api/v1/buyer/aftersales/${aftersaleId}/reverse-tracking`, {
        method: "POST",
        json: { carrierCode: carrier, trackingNo: tracking }
      });
      setMsg("已填写退货运单");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "填写失败");
    }
  }

  return (
    <PageShell title={`订单 #${orderId}`} subtitle="物流轨迹与售后">
      <p><Link to="/">返回首页</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {msg ? <p style={{ color: "green" }}>{msg}</p> : null}

      <section>
        <h2>物流</h2>
        {shipments.length === 0 ? <p>暂无运单</p> : null}
        {shipments.map((s) => (
          <div key={s.id} style={{ marginBottom: 12 }}>
            <b>{s.direction}</b> {s.carrierCode}/{s.trackingNo} · {s.status}
            <ol>
              {(s.tracks || []).map((t, i) => (
                <li key={i}>{t.trackedAt}: [{t.status}] {t.description}</li>
              ))}
            </ol>
          </div>
        ))}
      </section>

      <section>
        <h2>申请售后</h2>
        <form onSubmit={applyAftersale}>
          <select value={type} onChange={(e) => setType(e.target.value as "REFUND_ONLY" | "RETURN_REFUND")}>
            <option value="REFUND_ONLY">仅退款</option>
            <option value="RETURN_REFUND">退货退款</option>
          </select>{" "}
          <input value={reason} onChange={(e) => setReason(e.target.value)} placeholder="原因" />{" "}
          <input value={refundYuan} onChange={(e) => setRefundYuan(e.target.value)} placeholder="退款元" />{" "}
          <button type="submit">提交</button>
        </form>
      </section>

      <section>
        <h2>我的售后（本单相关请对照单号）</h2>
        <ul>
          {aftersales.map((a) => (
            <li key={a.id} style={{ marginBottom: 10 }}>
              {a.aftersaleNo} · {a.type} · {a.status} · ¥{(a.refundCents / 100).toFixed(2)}
              {a.type === "RETURN_REFUND" && a.status === "APPROVED" && !a.reverseShipmentId && (
                <div>
                  <input value={carrier} onChange={(e) => setCarrier(e.target.value)} placeholder="承运商" />{" "}
                  <input value={tracking} onChange={(e) => setTracking(e.target.value)} placeholder="退货运单号" />{" "}
                  <button type="button" onClick={() => fillReverse(a.id)}>填写逆向运单</button>
                </div>
              )}
            </li>
          ))}
        </ul>
      </section>
    </PageShell>
  );
}
