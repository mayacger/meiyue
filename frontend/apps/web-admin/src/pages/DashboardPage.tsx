import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, List, Spin, Typography } from "antd";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";

/**
 * 平台运营概览（I20）
 * API：GET /admin/dashboard · /admin/onboarding/pending
 * 指标：待审入驻 / 买家数 / 店主数 / 启用类目 / 平台券 / 在售商品
 */
interface AdminDashboard {
  pendingOnboardingCount: number;
  buyerUserCount: number;
  sellerUserCount: number;
  enabledCategoryCount: number;
  platformCouponCount: number;
  onSaleProductCount: number;
}

export function DashboardPage() {
  const { message } = App.useApp();
  const [stats, setStats] = useState<AdminDashboard | null>(null);
  const [pending, setPending] = useState<OnboardingApplication[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [d, p] = await Promise.all([
          apiFetch<AdminDashboard>("/api/v1/admin/dashboard"),
          apiFetch<OnboardingApplication[]>("/api/v1/admin/onboarding/pending")
        ]);
        setStats(d);
        setPending(p);
      } catch (e) {
        message.error(e instanceof Error ? e.message : "加载失败");
      } finally {
        setLoading(false);
      }
    })();
  }, [message]);

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  return (
    <PageContainer header={{ title: "运营概览", subTitle: "美月商城平台后台 · 无直播" }}>
      <StatisticCard.Group>
        <StatisticCard
          statistic={{ title: "待审入驻", value: stats?.pendingOnboardingCount ?? 0 }}
          extra={<Link to="/onboarding">去审核</Link>}
        />
        <StatisticCard
          statistic={{ title: "买家账号", value: stats?.buyerUserCount ?? 0 }}
          extra={<Link to="/account">账号</Link>}
        />
        <StatisticCard
          statistic={{ title: "店主账号", value: stats?.sellerUserCount ?? 0 }}
          extra={<Link to="/account">账号</Link>}
        />
        <StatisticCard
          statistic={{ title: "启用类目", value: stats?.enabledCategoryCount ?? 0 }}
          extra={<Link to="/categories">类目</Link>}
        />
        <StatisticCard
          statistic={{ title: "平台券", value: stats?.platformCouponCount ?? 0 }}
          extra={<Link to="/coupons">管理</Link>}
        />
        <StatisticCard statistic={{ title: "在售商品", value: stats?.onSaleProductCount ?? 0 }} />
      </StatisticCard.Group>
      <ProCard title="待审申请（最近）" style={{ marginTop: 16 }} extra={<Link to="/onboarding">全部</Link>}>
        <List
          dataSource={pending.slice(0, 5)}
          locale={{ emptyText: "暂无待审" }}
          renderItem={(item) => (
            <List.Item>
              <Typography.Text>
                #{item.id} {item.shopName}（{item.shopSlug}）
              </Typography.Text>
            </List.Item>
          )}
        />
      </ProCard>
    </PageContainer>
  );
}
