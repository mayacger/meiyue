import { LockOutlined, UserOutlined } from "@ant-design/icons";
import { LoginForm, ProFormText } from "@ant-design/pro-components";
import { App, Typography } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";

/** 商家登录：有 tenantId 进概览，否则进入驻 */
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
              json: { username: values.username, password: values.password }
            });
            setToken(data.accessToken);
            message.success("登录成功");
            navigate(data.user.tenantId ? "/" : "/onboarding");
          } catch (err) {
            message.error(err instanceof Error ? err.message : "登录失败");
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
        <Typography.Paragraph style={{ textAlign: "center" }}>
          还没有账号？<Link to="/register">注册商家</Link>
        </Typography.Paragraph>
      </LoginForm>
    </div>
  );
}
