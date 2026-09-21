import { View, Text, Button, Input, Picker } from "@tarojs/components";
import Taro, { useDidShow, useRouter } from "@tarojs/taro";
import { useState } from "react";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

interface OrderItem {
  id: number;
  productId: number;
  productTitle: string;
}

interface Order {
  id: number;
  orderNo: string;
  status: string;
  totalCents?: number;
  items: OrderItem[];
}

interface Track {
  status: string;
  description: string;
  trackedAt?: string;
  source?: string;
}

interface Shipment {
  id: number;
  status: string;
  carrierCode: string;
  trackingNo: string;
  packageSeq?: number;
  tracks?: Track[];
}

interface Aftersale {
  id: number;
  aftersaleNo: string;
  orderId: number;
  type: string;
  status: string;
  refundCents: number;
  reverseShipmentId?: number | null;
}

const CARRIERS = ["SF", "YTO", "ZTO", "STO", "YD", "JT"];

/**
 * 订单详情（I16 + I29）
 * 确认收货 / 申请售后（凭证图）/ 退货物流填写 / 物流摘要
 */
export default function OrderDetailPage() {
  const { params } = useRouter();
  const orderId = Number(params.id);
  const [order, setOrder] = useState<Order | null>(null);
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [aftersales, setAftersales] = useState<Aftersale[]>([]);
  const [typeIndex, setTypeIndex] = useState(0);
  const [reason, setReason] = useState("不想要了");
  const [refundYuan, setRefundYuan] = useState("99");
  const [evidenceUrl, setEvidenceUrl] = useState("");
  const [reviewItemId, setReviewItemId] = useState<number | null>(null);
  const [rating, setRating] = useState(5);
  const [reviewContent, setReviewContent] = useState("很好");
  const [carrierIndex, setCarrierIndex] = useState(0);
  const [trackingNo, setTrackingNo] = useState("");
  const types = [
    { label: "仅退款", value: "REFUND_ONLY" },
    { label: "退货退款", value: "RETURN_REFUND" }
  ];

  async function reload() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      return;
    }
    const o = await apiFetch<Order>(`/api/v1/buyer/orders/${orderId}`);
    setOrder(o);
    if (o.items?.[0] && reviewItemId == null) setReviewItemId(o.items[0].id);
    setShipments(await apiFetch<Shipment[]>(`/api/v1/buyer/orders/${orderId}/shipments`));
    const all = await apiFetch<Aftersale[]>("/api/v1/buyer/aftersales");
    setAftersales(all.filter((a) => a.orderId === orderId));
  }

  useDidShow(() => {
    if (!orderId) return;
    reload().catch((e) =>
      Taro.showToast({ title: e instanceof Error ? e.message : "加载失败", icon: "none" })
    );
  });

  async function confirmReceipt() {
    try {
      await apiFetch(`/api/v1/buyer/orders/${orderId}/confirm-receipt`, { method: "POST" });
      Taro.showToast({ title: "已确认收货", icon: "success" });
      await reload();
    } catch (e) {
      Taro.showToast({ title: e instanceof Error ? e.message : "失败", icon: "none" });
    }
  }

  async function applyAftersale() {
    try {
      const urls = evidenceUrl.trim() ? [evidenceUrl.trim()] : null;
      await apiFetch("/api/v1/buyer/aftersales", {
        method: "POST",
        data: {
          orderId,
          type: types[typeIndex].value,
          reason,
          refundCents: Math.round(parseFloat(refundYuan) * 100),
          evidenceImageUrls: urls
        }
      });
      Taro.showToast({ title: "售后已申请", icon: "success" });
      setEvidenceUrl("");
      await reload();
    } catch (e) {
      Taro.showToast({ title: e instanceof Error ? e.message : "申请失败", icon: "none" });
    }
  }

  async function fillReverse(asId: number) {
    if (!trackingNo.trim()) {
      Taro.showToast({ title: "请填写运单号", icon: "none" });
      return;
    }
    try {
      await apiFetch(`/api/v1/buyer/aftersales/${asId}/reverse-tracking`, {
        method: "POST",
        data: {
          carrierCode: CARRIERS[carrierIndex],
          trackingNo: trackingNo.trim(),
          remark: "买家寄回"
        }
      });
      Taro.showToast({ title: "退货运单已提交", icon: "success" });
      setTrackingNo("");
      await reload();
    } catch (e) {
      Taro.showToast({ title: e instanceof Error ? e.message : "提交失败", icon: "none" });
    }
  }

  async function submitReview() {
    if (!reviewItemId) {
      Taro.showToast({ title: "请选择商品行", icon: "none" });
      return;
    }
    try {
      await apiFetch("/api/v1/buyer/reviews", {
        method: "POST",
        data: {
          orderId,
          orderItemId: reviewItemId,
          rating,
          content: reviewContent
        }
      });
      Taro.showToast({ title: "评价已提交", icon: "success" });
    } catch (e) {
      Taro.showToast({ title: e instanceof Error ? e.message : "评价失败", icon: "none" });
    }
  }

  return (
    <View className="page">
      <Text className="h1">{order?.orderNo || `订单 #${orderId}`}</Text>
      <Text className="muted">状态：{order?.status}</Text>

      <View className="section">
        <Text className="h2">商品</Text>
        {order?.items?.map((it) => (
          <Text key={it.id} className="line">
            {it.productTitle}
          </Text>
        ))}
        {(order?.status === "SHIPPED" || order?.status === "DELIVERED") && (
          <Button className="btn" onClick={confirmReceipt}>
            确认收货
          </Button>
        )}
      </View>

      <View className="section">
        <Text className="h2">物流轨迹</Text>
        {shipments.length === 0 ? <Text className="muted">暂无物流</Text> : null}
        {shipments.map((s) => (
          <View key={s.id} className="ship">
            <Text className="line">
              包裹 #{s.packageSeq ?? s.id} · {s.carrierCode} {s.trackingNo} · {s.status}
            </Text>
            {(s.tracks || []).length === 0 ? (
              <Text className="muted">暂无轨迹节点</Text>
            ) : (
              [...(s.tracks || [])].reverse().map((t, i) => (
                <View key={`${t.status}-${i}`} className="track">
                  <Text className="track-status">{t.status}</Text>
                  <Text className="track-desc">{t.description}</Text>
                  <Text className="muted">
                    {t.trackedAt || ""}
                    {t.source ? ` · ${t.source}` : ""}
                  </Text>
                </View>
              ))
            )}
          </View>
        ))}
      </View>

      <View className="section">
        <Text className="h2">评价</Text>
        <Picker
          mode="selector"
          range={order?.items?.map((it) => it.productTitle) || []}
          value={Math.max(
            0,
            (order?.items || []).findIndex((it) => it.id === reviewItemId)
          )}
          onChange={(e) => {
            const it = order?.items?.[Number(e.detail.value)];
            if (it) setReviewItemId(it.id);
          }}
        >
          <View className="picker">
            商品行：
            {order?.items?.find((it) => it.id === reviewItemId)?.productTitle || "请选择"}
          </View>
        </Picker>
        <Picker
          mode="selector"
          range={["1", "2", "3", "4", "5"]}
          value={rating - 1}
          onChange={(e) => setRating(Number(e.detail.value) + 1)}
        >
          <View className="picker">评分：{rating} 星</View>
        </Picker>
        <Input
          className="input"
          value={reviewContent}
          onInput={(e) => setReviewContent(e.detail.value)}
          placeholder="评价内容"
        />
        <Button className="btn ghost" onClick={submitReview}>
          提交评价
        </Button>
      </View>

      <View className="section">
        <Text className="h2">申请售后</Text>
        <Picker
          mode="selector"
          range={types.map((t) => t.label)}
          value={typeIndex}
          onChange={(e) => setTypeIndex(Number(e.detail.value))}
        >
          <View className="picker">类型：{types[typeIndex].label}</View>
        </Picker>
        <Input className="input" value={reason} onInput={(e) => setReason(e.detail.value)} placeholder="原因" />
        <Input
          className="input"
          value={refundYuan}
          onInput={(e) => setRefundYuan(e.detail.value)}
          placeholder="退款金额（元）"
        />
        <Input
          className="input"
          value={evidenceUrl}
          onInput={(e) => setEvidenceUrl(e.detail.value)}
          placeholder="凭证图 URL（可选）"
        />
        <Button className="btn ghost" onClick={applyAftersale}>
          提交售后
        </Button>
        {aftersales.map((a) => (
          <View key={a.id} className="as-block">
            <Text className="line muted">
              {a.aftersaleNo} · {a.type} · {a.status} · ¥{(a.refundCents / 100).toFixed(2)}
            </Text>
            {a.type === "RETURN_REFUND" && a.status === "APPROVED" && !a.reverseShipmentId ? (
              <View>
                <Picker
                  mode="selector"
                  range={CARRIERS}
                  value={carrierIndex}
                  onChange={(e) => setCarrierIndex(Number(e.detail.value))}
                >
                  <View className="picker">承运商：{CARRIERS[carrierIndex]}</View>
                </Picker>
                <Input
                  className="input"
                  value={trackingNo}
                  onInput={(e) => setTrackingNo(e.detail.value)}
                  placeholder="退货运单号"
                />
                <Button className="btn" size="mini" onClick={() => fillReverse(a.id)}>
                  提交退货物流
                </Button>
              </View>
            ) : null}
            {a.reverseShipmentId ? (
              <Text className="muted">已填退货运单 #{a.reverseShipmentId}</Text>
            ) : null}
          </View>
        ))}
      </View>
    </View>
  );
}
