import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  reason: string;
  refundCents: number;
  reviewNote: string | null;
  reverseShipmentId: number | null;
}

/** 商家售后审核 */
export function AftersalesPage() {
  const [list, setList] = useState<Aftersale[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    setList(await apiFetch<Aftersale[]>("/api/v1/seller/aftersales"));
  }

  useEffect(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  async function approve(id: number) {
    await apiFetch(`/api/v1/seller/aftersales/${id}/approve`, {
      method: "POST",
      json: { reviewNote: "商家同意" }
    });
    await reload();
  }

  async function reject(id: number) {
    await apiFetch(`/api/v1/seller/aftersales/${id}/reject`, {
      method: "POST",
      json: { reviewNote: "商家拒绝" }
    });
    await reload();
  }

  async function confirmReturn(id: number) {
    await apiFetch(`/api/v1/seller/aftersales/${id}/confirm-return`, { method: "POST" });
    await reload();
  }

  return (
    <PageShell title="售后审核" subtitle="同意 / 拒绝 / 确认退货">
      <p><Link to="/">返回概览</Link></p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      <ul>
        {list.map((a) => (
          <li key={a.id} style={{ marginBottom: 12 }}>
            {a.aftersaleNo} · 订单{a.orderId} · {a.type} · <b>{a.status}</b>
            <br />
            原因：{a.reason} · 退款 ¥{(a.refundCents / 100).toFixed(2)}
            {a.reviewNote ? <> · 备注：{a.reviewNote}</> : null}
            <div>
              {(a.status === "REVIEWING" || a.status === "APPLIED") && (
                <>
                  <button type="button" onClick={() => approve(a.id).catch((e) => setError(e.message))}>同意</button>{" "}
                  <button type="button" onClick={() => reject(a.id).catch((e) => setError(e.message))}>拒绝</button>
                </>
              )}
              {a.type === "RETURN_REFUND" && a.status === "APPROVED" && a.reverseShipmentId && (
                <button type="button" onClick={() => confirmReturn(a.id).catch((e) => setError(e.message))}>
                  确认收到退货
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>
    </PageShell>
  );
}
