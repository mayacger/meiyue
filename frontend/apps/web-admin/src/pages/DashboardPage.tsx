import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, List, Spin, Typography } from "antd";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import type { CouponSummary, OnboardingApplication } from "@meiyue/types";

/**
 * 平台运营概览
 * 汇总待审入驻数、平台券数量；快捷入口
 */
export function DashboardPage() {
  const { message } = App.useApp();
  const [pending, setPending] = useState<OnboardingApplication[]>([]);
  const [coupons, setCoupons] = useState<CouponSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setPending(await apiFetch<OnboardingApplication[]>("/api/v1/admin/onboarding/pending"));
        setCoupons(await apiFetch<CouponSummary[]>("/api/v1/platform-coupons"));
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
          statistic={{ title: "待审入驻", value: pending.length }}
          extra={<Link to="/onboarding">去审核</Link>}
        />
        <StatisticCard
          statistic={{ title: "平台券", value: coupons.length }}
          extra={<Link to="/coupons">管理</Link>}
        />
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
