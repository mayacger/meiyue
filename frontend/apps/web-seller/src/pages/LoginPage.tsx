import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

/** 商家登录页 */
export function LoginPage() {
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        json: { username, password }
      });
      setToken(data.accessToken);
      // 已有店铺进概览，否则进入驻
      if (data.user.tenantId) {
        nav("/");
      } else {
        nav("/onboarding");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  }

  return (
    <PageShell title="商家登录" subtitle="美月商城商家后台">
      <form onSubmit={onSubmit} style={{ display: "grid", gap: 12, maxWidth: 360 }}>
        <label>
          用户名
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label>
          密码
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        <button type="submit">登录</button>
      </form>
      <p>
        还没有账号？<Link to="/register">注册商家账号</Link>
      </p>
    </PageShell>
  );
}
