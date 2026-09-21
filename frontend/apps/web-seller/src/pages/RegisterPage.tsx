import { LockOutlined, UserOutlined } from "@ant-design/icons";
import { LoginForm, ProFormText } from "@ant-design/pro-components";
import { App, Typography } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { AuthResult } from "@meiyue/types";

/**
 * 商家注册
 * API：POST /auth/register，actorType=SELLER
 */
export function RegisterPage() {
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
        title="注册商家账号"
        subTitle="注册后提交入驻申请"
        submitter={{ searchConfig: { submitText: "注册" } }}
        onFinish={async (values) => {
          try {
            const data = await apiFetch<AuthResult>("/api/v1/auth/register", {
              method: "POST",
              json: {
                username: values.username,
                password: values.password,
                displayName: values.displayName || values.username,
                actorType: "SELLER"
              }
            });
            setToken(data.accessToken);
            message.success("注册成功，请提交入驻");
            navigate("/onboarding");
          } catch (err) {
            message.error(err instanceof Error ? err.message : "注册失败");
          }
        }}
      >
        <ProFormText
          name="username"
          fieldProps={{ size: "large", prefix: <UserOutlined /> }}
          placeholder="用户名"
          rules={[{ required: true }]}
        />
        <ProFormText name="displayName" placeholder="显示名（可选）" fieldProps={{ size: "large" }} />
        <ProFormText.Password
          name="password"
          fieldProps={{ size: "large", prefix: <LockOutlined /> }}
          placeholder="密码"
          rules={[{ required: true }]}
        />
        <Typography.Paragraph style={{ textAlign: "center" }}>
          已有账号？<Link to="/login">去登录</Link>
        </Typography.Paragraph>
      </LoginForm>
    </div>
  );
}
