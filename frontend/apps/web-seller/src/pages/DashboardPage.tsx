import { useEffect, useState } from "react";
import { PageContainer, ProCard, StatisticCard } from "@ant-design/pro-components";
import { App, Button, Descriptions, Space, Spin } from "antd";
import { Link, useNavigate } from "react-router-dom";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import { apiFetch, downloadAuthenticated } from "@meiyue/api";
import type { StoreInfo, UserProfile } from "@meiyue/types";

/**
 * 商家经营概览（I20 + I27 图表）
 *
 * API：
 *   - GET /seller/dashboard
 *   - GET /seller/dashboard/series?days=7
 *   - GET /auth/me · /seller/store
 *
 * 指标：待发货 / 待售后 / 今日订单 / 今日销售额 / 低库存 SKU
 * 图表：近 7 日订单数 & 销售额（Recharts）
 */

interface SellerDashboard {
  pendingShipCount: number;
  pendingAftersaleCount: number;
  todayOrderCount: number;
  todaySalesCents: number;
  lowStockSkuCount: number;
}

/** 序列日点：date / orderCount / salesCents */
interface DayPoint {
  date: string;
  orderCount: number;
  salesCents: number;
}

interface DashboardSeries {
  days: DayPoint[];
}

export function DashboardPage() {
  const navigate = useNavigate();
  const { message, modal } = App.useApp();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [store, setStore] = useState<StoreInfo | null>(null);
  const [stats, setStats] = useState<SellerDashboard | null>(null);
  const [series, setSeries] = useState<DayPoint[]>([]);
  const [loading, setLoading] = useState(true);

  /** I36：销售报表 CSV（二次确认） */
  function exportSales(grain: "day" | "week", periods: number) {
    modal.confirm({
      title: grain === "day" ? "导出近7日销售报表？" : "导出近4周销售报表？",
      content: "含订单量、GMV、退款；请妥善保管。",
      okText: "确认导出",
      onOk: async () => {
        try {
          await downloadAuthenticated(
            `/api/v1/seller/dashboard/sales-report.csv?grain=${grain}&periods=${periods}`,
            `seller-sales-${grain}.csv`
          );
          message.success("已导出销售报表");
        } catch (err) {
          message.error(err instanceof Error ? err.message : "导出失败");
        }
      }
    });
  }

  useEffect(() => {
    (async () => {
      try {
        const profile = await apiFetch<UserProfile>("/api/v1/auth/me");
        setMe(profile);
        if (!profile.tenantId) {
          navigate("/onboarding");
          return;
        }
        const [s, d, ser] = await Promise.all([
          apiFetch<StoreInfo>("/api/v1/seller/store"),
          apiFetch<SellerDashboard>("/api/v1/seller/dashboard"),
          apiFetch<DashboardSeries>("/api/v1/seller/dashboard/series?days=7")
        ]);
        setStore(s);
        setStats(d);
        setSeries(ser.days || []);
      } catch (err) {
        message.error(err instanceof Error ? err.message : "加载失败");
      } finally {
        setLoading(false);
      }
    })();
  }, [navigate, message]);

  /** 图表用：销售额转元 */
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
    <PageContainer
      header={{
        title: "店铺概览",
        subTitle: "经营待办 · 今日表现 · 近7日趋势 · 销售报表",
        extra: (
          <Space>
            <Button onClick={() => exportSales("day", 7)}>导出日报 CSV</Button>
            <Button onClick={() => exportSales("week", 4)}>导出周报 CSV</Button>
          </Space>
        )
      }}
    >
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

      <ProCard title="近 7 日订单与销售额" style={{ marginTop: 16 }} bodyStyle={{ height: 320 }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e8e8e8" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis yAxisId="left" tick={{ fontSize: 12 }} allowDecimals={false} />
            <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 12 }} />
            <Tooltip />
            <Legend />
            <Line
              yAxisId="left"
              type="monotone"
              dataKey="orderCount"
              name="订单数"
              stroke="#0F3D38"
              strokeWidth={2}
              dot={{ r: 3 }}
            />
            <Line
              yAxisId="right"
              type="monotone"
              dataKey="salesYuan"
              name="销售额(元)"
              stroke="#C4A35A"
              strokeWidth={2}
              dot={{ r: 3 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </ProCard>

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
