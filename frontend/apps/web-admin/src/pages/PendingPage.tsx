import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

/** 待审入驻列表：通过 = 开店；拒绝可填备注 */
export function PendingPage() {
  const nav = useNavigate();
  const [list, setList] = useState<OnboardingApplication[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const reload = useCallback(() => {
    apiFetch<OnboardingApplication[]>("/api/v1/admin/onboarding/pending")
      .then(setList)
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  async function approve(id: number) {
    setBusyId(id);
    setError(null);
    try {
      await apiFetch(`/api/v1/admin/onboarding/${id}/approve`, {
        method: "POST",
        json: { reviewNote: "平台审核通过" }
      });
      reload();
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
      reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "操作失败");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <PageShell title="入驻审核" subtitle="待审申请列表（I1）">
      <p>
        <button
          type="button"
          onClick={() => {
            setToken(null);
            nav("/login");
          }}
        >
          退出
        </button>{" "}
        <button type="button" onClick={reload}>
          刷新
        </button>
      </p>
      {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
      {list.length === 0 ? (
        <p>暂无待审申请。</p>
      ) : (
        <ul style={{ listStyle: "none", padding: 0 }}>
          {list.map((app) => (
            <li key={app.id} style={{ borderTop: "1px solid #ddd", padding: "12px 0" }}>
              <strong>
                #{app.id} {app.shopName}
              </strong>{" "}
              <code>{app.shopSlug}</code>
              <div>
                联系人 {app.contactName} / {app.contactPhone} · 申请人 UID {app.applicantUserId}
              </div>
              <div style={{ marginTop: 8 }}>
                <button type="button" disabled={busyId === app.id} onClick={() => approve(app.id)}>
                  通过并开店
                </button>{" "}
                <button type="button" disabled={busyId === app.id} onClick={() => reject(app.id)}>
                  拒绝
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </PageShell>
  );
}
