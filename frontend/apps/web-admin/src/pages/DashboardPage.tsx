import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, List, Spin, Typography } from "antd";
import { Link } from "react-router-dom";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import { apiFetch } from "@meiyue/api";
import type { OnboardingApplication } from "@meiyue/types";

/**
 * 平台运营概览（I20 + I27 图表）
 *
 * API：GET /admin/dashboard · /admin/dashboard/series?days=7 · /admin/onboarding/pending
 * 指标：待审入驻 / 买家数 / 店主数 / 启用类目 / 平台券 / 在售商品
 * 图表：近 7 日平台订单与销售额（Recharts 柱状）
 */

interface AdminDashboard {
  pendingOnboardingCount: number;
  buyerUserCount: number;
  sellerUserCount: number;
  enabledCategoryCount: number;
  platformCouponCount: number;
  onSaleProductCount: number;
}

interface DayPoint {
  date: string;
  orderCount: number;
  salesCents: number;
}

interface DashboardSeries {
  days: DayPoint[];
}

export function DashboardPage() {
  const { message } = App.useApp();
  const [stats, setStats] = useState<AdminDashboard | null>(null);
  const [pending, setPending] = useState<OnboardingApplication[]>([]);
  const [series, setSeries] = useState<DayPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [d, p, ser] = await Promise.all([
          apiFetch<AdminDashboard>("/api/v1/admin/dashboard"),
          apiFetch<OnboardingApplication[]>("/api/v1/admin/onboarding/pending"),
          apiFetch<DashboardSeries>("/api/v1/admin/dashboard/series?days=7")
        ]);
        setStats(d);
        setPending(p);
        setSeries(ser.days || []);
      } catch (e) {
        message.error(e instanceof Error ? e.message : "加载失败");
      } finally {
        setLoading(false);
      }
    })();
  }, [message]);

  const chartData = series.map((p) => ({
    date: p.date.slice(5),
    orderCount: p.orderCount,
    salesYuan: Number((p.salesCents / 100).toFixed(2))
  }));

  if (loading) {
    return (
      <PageContainer>
        <Spin />
      </PageContainer>
    );
  }

  return (
    <PageContainer header={{ title: "运营概览", subTitle: "美月商城平台后台 · 近7日趋势 · 无直播" }}>
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

      <ProCard title="近 7 日平台订单与销售额" style={{ marginTop: 16 }} bodyStyle={{ height: 320 }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e8e8e8" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis yAxisId="left" tick={{ fontSize: 12 }} allowDecimals={false} />
            <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 12 }} />
            <Tooltip />
            <Legend />
            <Bar yAxisId="left" dataKey="orderCount" name="订单数" fill="#0F3D38" radius={[4, 4, 0, 0]} />
            <Bar yAxisId="right" dataKey="salesYuan" name="销售额(元)" fill="#C4A35A" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ProCard>

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
