import { useCallback, useEffect, useState } from "react";
import { LockOutlined, SafetyOutlined, UserOutlined } from "@ant-design/icons";
import { LoginForm, ProFormText } from "@ant-design/pro-components";
import { App, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult, CaptchaChallenge } from "@meiyue/types";

/**
 * 平台登录页（Pro LoginForm + I35 可选验证码）
 * 种子账号：admin / admin123；校验 PLATFORM_ADMIN 角色
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
        background: "linear-gradient(160deg, #E8ECE9 0%, #F7F6F3 45%, #D5E0DC 100%)"
      }}
    >
      <LoginForm
        title="美月商城"
        subTitle="平台运营后台"
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
            if (!data.user.roles.includes("PLATFORM_ADMIN")) {
              setToken(null);
              message.error("该账号不是平台管理员");
              return;
            }
            setToken(data.accessToken);
            message.success("登录成功");
            navigate("/onboarding");
          } catch (err) {
            message.error(err instanceof Error ? err.message : "登录失败");
            refreshCaptcha();
          }
        }}
      >
        <Typography.Paragraph type="secondary" style={{ textAlign: "center" }}>
          演示账号 admin / admin123
        </Typography.Paragraph>
        <ProFormText
          name="username"
          fieldProps={{ size: "large", prefix: <UserOutlined /> }}
          placeholder="用户名"
          initialValue="admin"
          rules={[{ required: true, message: "请输入用户名" }]}
        />
        <ProFormText.Password
          name="password"
          fieldProps={{ size: "large", prefix: <LockOutlined /> }}
          placeholder="密码"
          initialValue="admin123"
          rules={[{ required: true, message: "请输入密码" }]}
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
      </LoginForm>
    </div>
  );
}
