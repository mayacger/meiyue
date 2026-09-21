import { useCallback, useEffect, useState } from "react";
import { LockOutlined, SafetyOutlined, UserOutlined } from "@ant-design/icons";
import { LoginForm, ProFormText } from "@ant-design/pro-components";
import { App, Space, Typography } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult, CaptchaChallenge } from "@meiyue/types";

/**
 * 商家登录（I35 可选图形验证码）
 * 有 tenantId 进概览，否则进入驻
 */
export function LoginPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  /** I35：验证码挑战；enabled=false 时不渲染字段 */
  const [captcha, setCaptcha] = useState<CaptchaChallenge | null>(null);

  const refreshCaptcha = useCallback(async () => {
    try {
      setCaptcha(await apiFetch<CaptchaChallenge>("/api/v1/auth/captcha"));
    } catch {
      setCaptcha({ enabled: false, captchaId: null, imageBase64: null });
    }
  }, []);

  useEffect(() => {
    refreshCaptcha();
  }, [refreshCaptcha]);

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(160deg, #E8ECE9 0%, #F7F6F3 50%, #D5E0DC 100%)"
      }}
    >
      <LoginForm
        title="美月商城"
        subTitle="商家后台"
        onFinish={async (values) => {
          try {
            const data = await apiFetch<AuthResult>("/api/v1/auth/login", {
              method: "POST",
              json: {
                username: values.username,
                password: values.password,
                captchaId: captcha?.enabled ? captcha.captchaId : null,
                captchaCode: captcha?.enabled ? values.captchaCode : null
              }
            });
            setToken(data.accessToken);
            message.success("登录成功");
            navigate(data.user.tenantId ? "/" : "/onboarding");
          } catch (err) {
            message.error(err instanceof Error ? err.message : "登录失败");
            refreshCaptcha();
          }
        }}
      >
        <ProFormText
          name="username"
          fieldProps={{ size: "large", prefix: <UserOutlined /> }}
          placeholder="用户名"
          initialValue="seller1"
          rules={[{ required: true }]}
        />
        <ProFormText.Password
          name="password"
          fieldProps={{ size: "large", prefix: <LockOutlined /> }}
          placeholder="密码"
          initialValue="seller123"
          rules={[{ required: true }]}
        />
        {captcha?.enabled ? (
          <Space align="start" style={{ width: "100%", marginBottom: 16 }} size={12}>
            <ProFormText
              name="captchaCode"
              fieldProps={{ size: "large", prefix: <SafetyOutlined /> }}
              placeholder="验证码"
              rules={[{ required: true, message: "请输入验证码" }]}
              formItemProps={{ style: { flex: 1, marginBottom: 0 } }}
            />
            {captcha.imageBase64 ? (
              <img
                src={captcha.imageBase64}
                alt="验证码"
                width={120}
                height={40}
                style={{ cursor: "pointer", borderRadius: 6, border: "1px solid #d9d9d9" }}
                onClick={refreshCaptcha}
                title="点击刷新"
              />
            ) : null}
          </Space>
        ) : null}
        <Typography.Paragraph style={{ textAlign: "center" }}>
          还没有账号？<Link to="/register">注册商家</Link>
        </Typography.Paragraph>
      </LoginForm>
    </div>
  );
}
