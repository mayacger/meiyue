import { View, Text, Button, Input } from "@tarojs/components";
import Taro from "@tarojs/taro";
import { useState } from "react";
import "./index.css";

/**
 * 收货地址占位页（I16）
 * 后端地址簿 API 尚未开放：本地草稿占位，提示后续对接
 * 字段：name / phone / region / detail
 */
export default function AddressPage() {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [region, setRegion] = useState("上海市 · 浦东新区");
  const [detail, setDetail] = useState("");

  function saveLocal() {
    const draft = { name, phone, region, detail, updatedAt: Date.now() };
    Taro.setStorageSync("meiyue_address_draft", draft);
    Taro.showToast({ title: "已保存本地草稿", icon: "success" });
  }

  return (
    <View className="page">
      <Text className="h1">收货地址</Text>
      <Text className="muted">地址簿 API 待后端开放；本页为本地占位草稿，不参与下单校验。</Text>
      <Input className="input" placeholder="收件人" value={name} onInput={(e) => setName(e.detail.value)} />
      <Input className="input" placeholder="手机号" value={phone} onInput={(e) => setPhone(e.detail.value)} />
      <Input className="input" placeholder="省市区" value={region} onInput={(e) => setRegion(e.detail.value)} />
      <Input className="input" placeholder="详细地址" value={detail} onInput={(e) => setDetail(e.detail.value)} />
      <Button className="btn" onClick={saveLocal}>
        保存本地草稿
      </Button>
    </View>
  );
}
