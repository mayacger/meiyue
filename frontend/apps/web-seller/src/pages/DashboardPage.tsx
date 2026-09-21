import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, Descriptions, Spin } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import type { StoreInfo, UserProfile } from "@meiyue/types";

/**
 * 商家经营概览（I20）
 * API：GET /seller/dashboard · /auth/me · /seller/store
 * 指标：待发货 / 待售后 / 今日订单 / 今日销售额 / 低库存 SKU
 */
interface SellerDashboard {
  pendingShipCount: number;
  pendingAftersaleCount: number;
  todayOrderCount: number;
  todaySalesCents: number;
  lowStockSkuCount: number;
}

export function DashboardPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [stats, setStats] = useState<SellerDashboard | null>(null);
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
        const [s, d] = await Promise.all([
          apiFetch<StoreInfo>("/api/v1/seller/store"),
          apiFetch<SellerDashboard>("/api/v1/seller/dashboard")
        ]);
        setStore(s);
        setStats(d);
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
    <PageContainer header={{ title: "店铺概览", subTitle: "经营待办 · 今日表现" }}>
      <StatisticCard.Group>
        <StatisticCard
          statistic={{ title: "待发货", value: stats?.pendingShipCount ?? 0 }}
          extra={<Link to="/shipments">去发货</Link>}
        />
        <StatisticCard
          statistic={{ title: "待售后", value: stats?.pendingAftersaleCount ?? 0 }}
          extra={<Link to="/aftersales">去处理</Link>}
        />
        <StatisticCard statistic={{ title: "今日订单", value: stats?.todayOrderCount ?? 0 }} />
        <StatisticCard
          statistic={{
            title: "今日销售额",
            value: ((stats?.todaySalesCents ?? 0) / 100).toFixed(2),
            prefix: "¥"
          }}
        />
        <StatisticCard
          statistic={{ title: "低库存 SKU", value: stats?.lowStockSkuCount ?? 0 }}
          extra={<Link to="/inventory">库存</Link>}
        />
      </StatisticCard.Group>
      <ProCard title="店铺信息" style={{ marginTop: 16 }} extra={<Link to="/inventory">库存管理</Link>}>
        {store ? (
          <Descriptions column={2}>
            <Descriptions.Item label="名称">{store.name}</Descriptions.Item>
            <Descriptions.Item label="Slug">{store.slug}</Descriptions.Item>
            <Descriptions.Item label="状态">{store.status}</Descriptions.Item>
            <Descriptions.Item label="租户 ID">{me?.tenantId}</Descriptions.Item>
            <Descriptions.Item label="账号">{me?.username}</Descriptions.Item>
            <Descriptions.Item label="角色">{me?.roles.join(", ")}</Descriptions.Item>
          </Descriptions>
        ) : (
          <p>暂无店铺</p>
        )}
      </ProCard>
    </PageContainer>
  );
}
