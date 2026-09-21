import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";
import "./LoginPage.css";

/** 买家登录：buyer1 / buyer123（demo） */
export function LoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
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
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  }

  return (
    <div className="my-login">
      <form className="my-login__panel my-fade-up" onSubmit={onSubmit}>
        <p className="my-brand__mark">美月商城</p>
        <h1>登录</h1>
        <label>
          用户名
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label>
          密码
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        {error ? <p className="my-error">{error}</p> : null}
        <button type="submit" className="my-btn my-btn--primary">
          登录
        </button>
        <p className="my-muted">
          <Link to="/">返回首页</Link>
        </p>
      </form>
    </div>
  );
}
