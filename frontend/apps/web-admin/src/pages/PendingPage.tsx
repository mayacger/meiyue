import { FormEvent, useEffect, useState } from "react";
import { apiFetch, setToken } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";
import type { OnboardingApplication } from "@meiyue/types";

interface PlatformCoupon {
  id: number;
  code: string;
  title: string;
  discountCents: number;
  minSpendCents: number;
  totalQuota: number;
  claimedCount: number;
  status: string;
}

/** 平台后台：入驻审核 + 平台券发放 */
export function PendingPage() {
  const [list, setList] = useState<OnboardingApplication[]>([]);
  const [coupons, setCoupons] = useState<PlatformCoupon[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [code, setCode] = useState("PLAT10");
  const [title, setTitle] = useState("平台满减10元");
  const [discountYuan, setDiscountYuan] = useState("10");
  const [minYuan, setMinYuan] = useState("50");
  const [quota, setQuota] = useState("1000");
  const [notifyUserId, setNotifyUserId] = useState("3");
  const [notifyTitle, setNotifyTitle] = useState("平台活动");
  const [notifyBody, setNotifyBody] = useState("您有一张平台券可领");

  async function reload() {
    setList(await apiFetch<OnboardingApplication[]>("/api/v1/admin/onboarding/pending"));
    setCoupons(await apiFetch<PlatformCoupon[]>("/api/v1/platform-coupons"));
  }

  useEffect(() => {
    reload().catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, []);

  async function approve(id: number) {
    setBusyId(id);
    setError(null);
    try {
      await apiFetch(`/api/v1/admin/onboarding/${id}/approve`, {
        method: "POST",
        json: { reviewNote: "平台审核通过" }
      });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "操作失败");
    } finally {
      setBusyId(null);
    }
  }

  async function reject(id: number) {
    const note = window.prompt("拒绝原因", "资料不完整") ?? "";
    setBusyId(id);
    setError(null);
    try {
      await apiFetch(`/api/v1/admin/onboarding/${id}/reject`, {
        method: "POST",
        json: { reviewNote: note }
      });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "操作失败");
    } finally {
      setBusyId(null);
    }
  }

  async function createCoupon(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/admin/platform-coupons", {
        method: "POST",
        json: {
          code,
          title,
          discountCents: Math.round(parseFloat(discountYuan) * 100),
          minSpendCents: Math.round(parseFloat(minYuan) * 100),
          totalQuota: parseInt(quota, 10)
        }
      });
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "发券失败");
    }
  }

  async function sendNotify(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/admin/notifications", {
        method: "POST",
        json: {
          userId: Number(notifyUserId),
          audience: "BUYER",
          title: notifyTitle,
          body: notifyBody,
          category: "COUPON"
        }
      });
      alert("通知已写入");
    } catch (err) {
      setError(err instanceof Error ? err.message : "发送失败");
    }
  }

  return (
    <PageShell title="平台后台" subtitle="入驻审核 · 平台券 · 站内通知（I1/I10）">
      <p>
        <button type="button" onClick={() => { setToken(null); window.location.href = "/login"; }}>退出</button>{" "}
        <button type="button" onClick={() => reload().catch(() => undefined)}>刷新</button>
      </p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}

      <section>
        <h2>平台券发放</h2>
        <p style={{ fontSize: 13 }}>与店券默认互斥（MUTUAL_EXCLUSIVE），下单不可同时抵扣</p>
        <form onSubmit={createCoupon}>
          <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="券码" required />{" "}
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="标题" required />{" "}
          <input value={discountYuan} onChange={(e) => setDiscountYuan(e.target.value)} placeholder="面额元" />{" "}
          <input value={minYuan} onChange={(e) => setMinYuan(e.target.value)} placeholder="门槛元" />{" "}
          <input value={quota} onChange={(e) => setQuota(e.target.value)} placeholder="总量" />{" "}
          <button type="submit">创建平台券</button>
        </form>
        <ul>
          {coupons.map((c) => (
            <li key={c.id}>
              {c.code} · {c.title} · 减¥{(c.discountCents / 100).toFixed(2)} · 满¥{(c.minSpendCents / 100).toFixed(2)}
              · 已领 {c.claimedCount}/{c.totalQuota || "∞"} · {c.status}
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2>站内通知（写入骨架）</h2>
        <form onSubmit={sendNotify}>
          <input value={notifyUserId} onChange={(e) => setNotifyUserId(e.target.value)} placeholder="userId" />{" "}
          <input value={notifyTitle} onChange={(e) => setNotifyTitle(e.target.value)} placeholder="标题" />{" "}
          <input value={notifyBody} onChange={(e) => setNotifyBody(e.target.value)} placeholder="正文" />{" "}
          <button type="submit">发送</button>
        </form>
      </section>

      <section>
        <h2>入驻审核</h2>
        {list.length === 0 ? (
          <p>暂无待审申请。</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0 }}>
            {list.map((app) => (
              <li key={app.id} style={{ borderTop: "1px solid #ddd", padding: "12px 0" }}>
                <strong>#{app.id} {app.shopName}</strong> <code>{app.shopSlug}</code>
                <div>联系人 {app.contactName} / {app.contactPhone} · 申请人 UID {app.applicantUserId}</div>
                <div style={{ marginTop: 8 }}>
                  <button type="button" disabled={busyId === app.id} onClick={() => approve(app.id)}>通过并开店</button>{" "}
                  <button type="button" disabled={busyId === app.id} onClick={() => reject(app.id)}>拒绝</button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </PageShell>
  );
}
