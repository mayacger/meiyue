import { LockOutlined, UserOutlined } from "@ant-design/icons";
import { LoginForm, ProFormText } from "@ant-design/pro-components";
import { App, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";

/**
 * 平台登录页（Pro LoginForm）
 * 种子账号：admin / admin123；校验 PLATFORM_ADMIN 角色
 */
export function LoginPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();

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
                password: values.password
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
      </LoginForm>
    </div>
  );
}
