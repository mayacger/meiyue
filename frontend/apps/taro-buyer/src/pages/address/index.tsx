import { View, Text, Button, Input, Switch } from "@tarojs/components";
import Taro, { useDidShow } from "@tarojs/taro";
import { useState } from "react";
import type { BuyerAddress } from "@meiyue/types";
import { apiFetch, getToken } from "../../services/api";
import "./index.css";

/**
 * 收货地址簿（I18）
 * 入口：我的 → 收货地址 · pages/address/index
 * API：CRUD /buyer/addresses · 设默认
 * 字段：receiverName / receiverPhone / province / city / district /
 * detailAddress / defaultAddress
 * 替换原本地草稿 storage。
 */
export default function AddressPage() {
  const [list, setList] = useState<BuyerAddress[]>([]);
  const [error, setError] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [province, setProvince] = useState("上海市");
  const [city, setCity] = useState("上海市");
  const [district, setDistrict] = useState("浦东新区");
  const [detailAddress, setDetailAddress] = useState("");
  const [defaultAddress, setDefaultAddress] = useState(false);

  async function reload() {
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      return;
    }
    const data = await apiFetch<BuyerAddress[]>("/api/v1/buyer/addresses");
    setList(data);
  }

  useDidShow(() => {
    reload().catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  });

  function resetForm() {
    setEditingId(null);
    setReceiverName("");
    setReceiverPhone("");
    setProvince("上海市");
    setCity("上海市");
    setDistrict("浦东新区");
    setDetailAddress("");
    setDefaultAddress(false);
  }

  function fillForm(a: BuyerAddress) {
    setEditingId(a.id);
    setReceiverName(a.receiverName);
    setReceiverPhone(a.receiverPhone);
    setProvince(a.province);
    setCity(a.city);
    setDistrict(a.district);
    setDetailAddress(a.detailAddress);
    setDefaultAddress(a.defaultAddress);
  }

  async function save() {
    setError("");
    if (!getToken()) {
      Taro.showToast({ title: "请先登录", icon: "none" });
      return;
    }
    const body = {
      receiverName,
      receiverPhone,
      province,
      city,
      district,
      detailAddress,
      defaultAddress
    };
    try {
      if (editingId) {
        await apiFetch(`/api/v1/buyer/addresses/${editingId}`, { method: "PUT", data: body });
      } else {
        await apiFetch("/api/v1/buyer/addresses", { method: "POST", data: body });
      }
      Taro.showToast({ title: "已保存", icon: "success" });
      resetForm();
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "保存失败");
    }
  }

  async function setDefault(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/addresses/${id}/default`, { method: "POST" });
      Taro.showToast({ title: "已设默认", icon: "success" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "设置失败");
    }
  }

  async function remove(id: number) {
    try {
      await apiFetch(`/api/v1/buyer/addresses/${id}`, { method: "DELETE" });
      if (editingId === id) resetForm();
      Taro.showToast({ title: "已删除", icon: "success" });
      await reload();
    } catch (e) {
      setError(e instanceof Error ? e.message : "删除失败");
    }
  }

  return (
    <View className="page">
      <Text className="h1">收货地址</Text>
      <Text className="muted">对接 /buyer/addresses 真实 API，登录后可增删改与设默认。</Text>
      {error ? <Text className="err">{error}</Text> : null}

      <Input
        className="input"
        placeholder="收件人"
        value={receiverName}
        onInput={(e) => setReceiverName(e.detail.value)}
      />
      <Input
        className="input"
        placeholder="手机号"
        value={receiverPhone}
        onInput={(e) => setReceiverPhone(e.detail.value)}
      />
      <Input
        className="input"
        placeholder="省"
        value={province}
        onInput={(e) => setProvince(e.detail.value)}
      />
      <Input className="input" placeholder="市" value={city} onInput={(e) => setCity(e.detail.value)} />
      <Input
        className="input"
        placeholder="区县"
        value={district}
        onInput={(e) => setDistrict(e.detail.value)}
      />
      <Input
        className="input"
        placeholder="详细地址"
        value={detailAddress}
        onInput={(e) => setDetailAddress(e.detail.value)}
      />
      <View className="switch-row">
        <Text>设为默认</Text>
        <Switch checked={defaultAddress} onChange={(e) => setDefaultAddress(!!e.detail.value)} />
      </View>
      <Button className="btn" onClick={save}>
        {editingId ? "保存修改" : "新增地址"}
      </Button>
      {editingId ? (
        <Button className="btn btn-ghost" onClick={resetForm}>
          取消编辑
        </Button>
      ) : null}

      <Text className="section">已保存（{list.length}）</Text>
      {list.length === 0 ? <Text className="muted">暂无地址</Text> : null}
      {list.map((a) => (
        <View key={a.id} className="card">
          <Text className="card-title">
            {a.receiverName} · {a.receiverPhone}
            {a.defaultAddress ? " · 默认" : ""}
          </Text>
          <Text className="muted">
            {a.province}
            {a.city}
            {a.district} {a.detailAddress}
          </Text>
          <View className="ops">
            <Button size="mini" onClick={() => fillForm(a)}>
              编辑
            </Button>
            {!a.defaultAddress ? (
              <Button size="mini" onClick={() => setDefault(a.id)}>
                设默认
              </Button>
            ) : null}
            <Button size="mini" onClick={() => remove(a.id)}>
              删除
            </Button>
          </View>
        </View>
      ))}
    </View>
  );
}
