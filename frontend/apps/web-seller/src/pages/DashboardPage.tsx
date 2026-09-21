import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, Descriptions, Spin } from "antd";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import type { StoreInfo, UserProfile } from "@meiyue/types";

/**
 * 商家概览 Dashboard
 * 拉取 /auth/me 与 /seller/store；无租户跳转入驻
 */
export function DashboardPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const profile = await apiFetch<UserProfile>("/api/v1/auth/me");
        setMe(profile);
        if (!profile.tenantId) {
          navigate("/onboarding");
          return;
        }
        setStore(await apiFetch<StoreInfo>("/api/v1/seller/store"));
      } catch (err) {
        message.error(err instanceof Error ? err.message : "加载失败");
      } finally {
        setLoading(false);
      }
    })();
  }, [navigate, message]);

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  return (
    <PageContainer header={{ title: "店铺概览", subTitle: "美月商城商家后台" }}>
      <StatisticCard.Group>
        <StatisticCard statistic={{ title: "租户 ID", value: me?.tenantId ?? "-" }} />
        <StatisticCard statistic={{ title: "店铺 ID", value: store?.id ?? "-" }} />
        <StatisticCard statistic={{ title: "店铺状态", value: store?.status ?? "-" }} />
      </StatisticCard.Group>
      <ProCard title="当前账号" style={{ marginTop: 16 }}>
        {me ? (
          <Descriptions column={2}>
            <Descriptions.Item label="用户名">{me.username}</Descriptions.Item>
            <Descriptions.Item label="显示名">{me.displayName}</Descriptions.Item>
            <Descriptions.Item label="角色">{me.roles.join(", ")}</Descriptions.Item>
            <Descriptions.Item label="Actor">{me.actorType}</Descriptions.Item>
          </Descriptions>
        ) : null}
      </ProCard>
      <ProCard title="我的店铺" style={{ marginTop: 16 }}>
        {store ? (
          <Descriptions column={2}>
            <Descriptions.Item label="名称">{store.name}</Descriptions.Item>
            <Descriptions.Item label="Slug">{store.slug}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{store.createdAt}</Descriptions.Item>
          </Descriptions>
        ) : (
          <p>暂无店铺</p>
        )}
      </ProCard>
    </PageContainer>
  );
}
