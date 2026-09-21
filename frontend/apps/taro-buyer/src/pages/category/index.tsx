import { View, Text } from "@tarojs/components";
import { useEffect, useState } from "react";
import { apiFetch } from "../../services/api";
import "./index.css";

interface Category {
  id: number;
  name: string;
}

/** 分类页骨架：拉取平台统一类目 */
export default function CategoryPage() {
  const [list, setList] = useState<Category[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    apiFetch<Category[]>("/api/v1/categories")
      .then(setList)
      .catch((e) => setError(e instanceof Error ? e.message : "加载失败"));
  }, []);

  return (
    <View className="page">
      <Text className="h1">分类</Text>
      {error ? <Text className="err">{error}</Text> : null}
      {list.map((c) => (
        <View key={c.id} className="row">
          <Text>{c.name}</Text>
        </View>
      ))}
      {list.length === 0 && !error ? <Text className="muted">暂无类目</Text> : null}
    </View>
  );
}
