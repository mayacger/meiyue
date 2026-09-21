import { useEffect, useState } from "react";
import {
  PageContainer,
  ProForm,
  ProFormText
} from "@ant-design/pro-components";
import { App, Alert, Card, Tag } from "antd";
import { useNavigate } from "react-router-dom";
import { apiFetch, setToken } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";

/**
 * 商家入驻申请页
 * GET /seller/onboarding/me · POST /seller/onboarding/apply
 * 字段：shopName / shopSlug / contactName / contactPhone
 */
export function OnboardingPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [current, setCurrent] = useState<OnboardingApplication | null>(null);

  useEffect(() => {
    apiFetch<OnboardingApplication | null>("/api/v1/seller/onboarding/me")
      .then((app) => {
        if (app?.status === "APPROVED") navigate("/");
        setCurrent(app);
      })
      .catch(() => undefined);
  }, [navigate]);

  return (
    <PageContainer
      header={{
        title: "店铺入驻",
        subTitle: "提交后等待平台审核",
        extra: [
          <a
            key="logout"
            onClick={() => {
              setToken(null);
              navigate("/login");
            }}
          >
            退出
          </a>
        ]
      }}
    >
      {current ? (
        <Alert
          style={{ marginBottom: 16 }}
          type={current.status === "REJECTED" ? "error" : "info"}
          message={`当前申请 #${current.id} · ${current.shopName} · ${current.status}`}
          description={current.reviewNote || undefined}
        />
      ) : null}
      <Card>
        <ProForm
          onFinish={async (values) => {
            try {
              await apiFetch("/api/v1/seller/onboarding/apply", {
                method: "POST",
                json: values
              });
              message.success("已提交申请");
              const app = await apiFetch<OnboardingApplication>("/api/v1/seller/onboarding/me");
              setCurrent(app);
              return true;
            } catch (err) {
              message.error(err instanceof Error ? err.message : "提交失败");
              return false;
            }
          }}
        >
          <ProFormText name="shopName" label="店铺名称" rules={[{ required: true }]} />
          <ProFormText
            name="shopSlug"
            label="店铺 slug"
            extra="仅小写字母数字与连字符"
            rules={[{ required: true }]}
          />
          <ProFormText name="contactName" label="联系人" rules={[{ required: true }]} />
          <ProFormText name="contactPhone" label="联系电话" rules={[{ required: true }]} />
        </ProForm>
        {current?.status === "PENDING" ? <Tag color="processing">审核中，请耐心等待</Tag> : null}
      </Card>
    </PageContainer>
  );
}
