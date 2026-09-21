import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, getToken } from "@meiyue/api";
import { ErrorState } from "@meiyue/ui";
import type { UserProfile } from "@meiyue/types";
import "./SettingsPage.css";

/**
 * 买家个人设置（I24）
 * 入口：顶栏「设置」· /settings
 * API：GET /auth/me · PUT /auth/profile · POST /auth/password
 */
export function SettingsPage() {
  const navigate = useNavigate();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [displayName, setDisplayName] = useState("");
  const [phone, setPhone] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!getToken()) {
      navigate("/login");
      return;
    }
    apiFetch<UserProfile>("/api/v1/auth/me")
      .then((u) => {
        setMe(u);
        setDisplayName(u.displayName || "");
        setPhone(u.phone || "");
      })
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, [navigate]);

  async function saveProfile(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      const u = await apiFetch<UserProfile>("/api/v1/auth/profile", {
        method: "PUT",
        json: { displayName, phone }
      });
      setMe(u);
      setMsg("资料已保存");
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    }
  }

  async function changePassword(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      await apiFetch("/api/v1/auth/password", {
        method: "POST",
        json: { oldPassword, newPassword }
      });
      setOldPassword("");
      setNewPassword("");
      setMsg("密码已更新，请牢记新密码");
    } catch (err) {
      setError(err instanceof Error ? err.message : "改密失败");
    }
  }

  return (
    <div className="my-settings my-page">
      <h1 className="my-page-title my-fade-up">个人设置</h1>
      <p className="my-page-lead">
        账号 {me?.username || "…"} · <Link to="/orders">我的订单</Link>
      </p>
      {msg ? <p className="my-ok">{msg}</p> : null}
      {error ? <ErrorState message={error} /> : null}

      <form className="my-settings__form my-fade-up" onSubmit={saveProfile}>
        <h2>资料</h2>
        <label>
          展示名
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required maxLength={128} />
        </label>
        <label>
          手机
          <input value={phone} onChange={(e) => setPhone(e.target.value)} maxLength={32} placeholder="选填" />
        </label>
        <button type="submit" className="my-btn my-btn--primary">
          保存资料
        </button>
      </form>

      <form className="my-settings__form my-fade-up-delay" onSubmit={changePassword}>
        <h2>修改密码</h2>
        <label>
          当前密码
          <input
            type="password"
            value={oldPassword}
            onChange={(e) => setOldPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>
        <label>
          新密码（至少 6 位）
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            minLength={6}
            autoComplete="new-password"
          />
        </label>
        <button type="submit" className="my-btn my-btn--ghost">
          更新密码
        </button>
      </form>
    </div>
  );
}
