import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

/** 平台管理员登录（种子：admin / admin123） */
export function LoginPage() {
  const nav = useNavigate();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        json: { username, password }
      });
      if (!data.user.roles.includes("PLATFORM_ADMIN")) {
        setToken(null);
        setError("该账号不是平台管理员");
        return;
      }
      setToken(data.accessToken);
      nav("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  }

  return (
    <PageShell title="平台登录" subtitle="美月商城运营后台">
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
    </PageShell>
  );
}
