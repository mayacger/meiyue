import { FormEvent, useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult, CaptchaChallenge } from "@meiyue/types";
import "./LoginPage.css";

/**
 * 买家登录（I35 可选图形验证码）
 * 种子：buyer1 / buyer123
 * API：GET /auth/captcha · POST /auth/login
 */
export function LoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("buyer1");
  const [password, setPassword] = useState("buyer123");
  /** I35：验证码挑战；enabled=false 时不展示输入框 */
  const [captcha, setCaptcha] = useState<CaptchaChallenge | null>(null);
  const [captchaCode, setCaptchaCode] = useState("");
  const [error, setError] = useState<string | null>(null);

  const refreshCaptcha = useCallback(async () => {
    try {
      const c = await apiFetch<CaptchaChallenge>("/api/v1/auth/captcha");
      setCaptcha(c);
      setCaptchaCode("");
    } catch {
      setCaptcha({ enabled: false, captchaId: null, imageBase64: null });
    }
  }, []);

  useEffect(() => {
    refreshCaptcha();
  }, [refreshCaptcha]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
        method: "POST",
        json: {
          username,
          password,
          // 关闭时传 null，后端 CaptchaService 直接跳过
          captchaId: captcha?.enabled ? captcha.captchaId : null,
          captchaCode: captcha?.enabled ? captchaCode : null
        }
      });
      setToken(data.accessToken);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
      // 失败后刷新验证码，避免复用已消费挑战
      refreshCaptcha();
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
        {captcha?.enabled ? (
          <div className="my-login__captcha">
            <label>
              验证码
              <input
                value={captchaCode}
                onChange={(e) => setCaptchaCode(e.target.value)}
                required
                maxLength={8}
                autoComplete="off"
                placeholder="图中字符"
              />
            </label>
            {captcha.imageBase64 ? (
              <button
                type="button"
                className="my-login__captcha-img"
                onClick={refreshCaptcha}
                title="点击刷新"
                aria-label="刷新验证码"
              >
                <img src={captcha.imageBase64} alt="验证码" width={120} height={40} />
              </button>
            ) : null}
          </div>
        ) : null}
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
