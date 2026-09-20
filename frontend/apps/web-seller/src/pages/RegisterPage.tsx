import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";
import { PageShell } from "@meiyue/ui";

/** 商家注册（自助仅 BUYER；入驻审核通过后升为店主） */
export function RegisterPage() {
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/register", {
        method: "POST",
        json: {
          username,
          password,
          displayName,
          phone,
          role: "SELLER_OWNER"
        }
      });
      setToken(data.accessToken);
      nav("/onboarding");
    } catch (err) {
      setError(err instanceof Error ? err.message : "注册失败");
    }
  }

  return (
    <PageShell title="商家注册" subtitle="注册后提交入驻申请，平台审核通过即可开店">
      <form onSubmit={onSubmit} style={{ display: "grid", gap: 12, maxWidth: 360 }}>
        <label>
          用户名
          <input value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
        </label>
        <label>
          密码
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
        </label>
        <label>
          展示名
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required />
        </label>
        <label>
          手机
          <input value={phone} onChange={(e) => setPhone(e.target.value)} />
        </label>
        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        <button type="submit">注册并去入驻</button>
      </form>
      <p>
        已有账号？<Link to="/login">登录</Link>
      </p>
    </PageShell>
  );
}
