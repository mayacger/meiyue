import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

/** 入驻申请页：提交 / 查看最新状态 */
export function OnboardingPage() {
  const nav = useNavigate();
  const [existing, setExisting] = useState<OnboardingApplication | null>(null);
  const [shopName, setShopName] = useState("");
  const [shopSlug, setShopSlug] = useState("");
  const [contactName, setContactName] = useState("");
  const [contactPhone, setContactPhone] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<OnboardingApplication>("/api/v1/seller/onboarding/me")
      .then((app) => {
        setExisting(app);
        if (app.status === "APPROVED") {
          nav("/");
        }
      })
      .catch(() => {
        // 404 表示尚无申请
      });
  }, [nav]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const app = await apiFetch<OnboardingApplication>("/api/v1/seller/onboarding/apply", {
        method: "POST",
        json: { shopName, shopSlug, contactName, contactPhone }
      });
      setExisting(app);
      setMsg("已提交，等待平台审核");
    } catch (err) {
      setError(err instanceof Error ? err.message : "提交失败");
    }
  }

  return (
    <PageShell title="商家入驻" subtitle="提交后由平台审核；通过即自动开店（1 租户 : 1 店铺）">
      <p>
        <button type="button" onClick={() => { setToken(null); nav("/login"); }}>
          退出登录
        </button>
      </p>
      {existing ? (
        <section>
          <h2>当前申请</h2>
          <pre>{JSON.stringify(existing, null, 2)}</pre>
          {existing.status === "PENDING" ? <p>审核中，请耐心等待。</p> : null}
          {existing.status === "REJECTED" ? <p>已拒绝：{existing.reviewNote}</p> : null}
        </section>
      ) : (
        <form onSubmit={onSubmit} style={{ display: "grid", gap: 12, maxWidth: 400 }}>
          <label>
            店铺名称
            <input value={shopName} onChange={(e) => setShopName(e.target.value)} required />
          </label>
          <label>
            店铺 slug（小写字母数字短横线）
            <input value={shopSlug} onChange={(e) => setShopSlug(e.target.value)} required pattern="^[a-z0-9]+(?:-[a-z0-9]+)*$" />
          </label>
          <label>
            联系人
            <input value={contactName} onChange={(e) => setContactName(e.target.value)} required />
          </label>
          <label>
            联系电话
            <input value={contactPhone} onChange={(e) => setContactPhone(e.target.value)} required />
          </label>
          {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
          {msg ? <p>{msg}</p> : null}
          <button type="submit">提交入驻申请</button>
        </form>
      )}
    </PageShell>
  );
}
