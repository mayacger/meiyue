import { useCallback, useEffect, useState } from "react";
import {
  PageContainer,
  ProCard,
  ProForm,
  ProFormSelect,
  ProFormText,
  ProTable
} from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import { App, Button, Space, Switch, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

interface Template {
  code: string;
  name: string;
  description: string;
  defaultFloorsJson: string;
}

interface StorePage {
  id: number;
  templateCode: string;
  themeColor: string;
  floorsJson: string;
  status: string;
}

/** 楼层配置项：type / sortOrder / enabled / title 等 */
interface FloorItem {
  type: string;
  sortOrder: number;
  enabled?: boolean;
  title?: string;
  imageUrl?: string;
  body?: string;
  [key: string]: unknown;
}

const FLOOR_TYPES = [
  "BANNER",
  "CATEGORY_NAV",
  "PRODUCT_RECOMMEND",
  "PRODUCT_GROUP",
  "IMAGE_TEXT",
  "IMAGE_STRIP",
  "RICH_TEXT",
  "COUPON_ENTRY"
];

function parseFloors(json: string): FloorItem[] {
  try {
    const arr = JSON.parse(json || "[]");
    if (!Array.isArray(arr)) return [];
    return arr.map((f: FloorItem, i: number) => ({
      ...f,
      type: String(f.type || "BANNER").toUpperCase(),
      sortOrder: typeof f.sortOrder === "number" ? f.sortOrder : (i + 1) * 10,
      enabled: f.enabled !== false
    }));
  } catch {
    return [];
  }
}

/**
 * 店铺装修可视化（I16）
 * 模板 + 主题色 + 楼层增删改排序；禁止直播楼层
 * API：templates / draft PUT / publish
 */
export function DecorationPage() {
  const { message } = App.useApp();
  const [templates, setTemplates] = useState<Template[]>([]);
  const [draft, setDraft] = useState<StorePage | null>(null);
  const [templateCode, setTemplateCode] = useState("simple_banner");
  const [themeColor, setThemeColor] = useState("#1A4D45");
  const [floors, setFloors] = useState<FloorItem[]>([]);

  const reload = useCallback(async () => {
    const list = await apiFetch<Template[]>("/api/v1/decoration/templates");
    setTemplates(list);
    try {
      const d = await apiFetch<StorePage>("/api/v1/seller/decoration/draft");
      setDraft(d);
      setTemplateCode(d.templateCode);
      setThemeColor(d.themeColor || "#1A4D45");
      setFloors(parseFloors(d.floorsJson));
    } catch {
      setDraft(null);
      if (list[0]) {
        setTemplateCode(list[0].code);
        setFloors(parseFloors(list[0].defaultFloorsJson));
      }
    }
  }, []);

  useEffect(() => {
    reload().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
  }, [reload, message]);

  async function saveDraft() {
    // 过滤直播相关类型（硬约束）
    const safe = floors
      .filter((f) => !String(f.type).toUpperCase().includes("LIVE"))
      .map((f, i) => ({ ...f, sortOrder: (i + 1) * 10 }));
    await apiFetch("/api/v1/seller/decoration/draft", {
      method: "PUT",
      json: {
        templateCode,
        themeColor,
        floorsJson: JSON.stringify(safe)
      }
    });
    message.success("草稿已保存");
    await reload();
  }

  const floorCols: ProColumns<FloorItem>[] = [
    {
      title: "排序",
      dataIndex: "sortOrder",
      width: 80,
      render: (_, __, index) => index + 1
    },
    {
      title: "类型",
      dataIndex: "type",
      render: (_, r) => <Tag color="blue">{r.type}</Tag>
    },
    {
      title: "标题",
      dataIndex: "title",
      render: (_, r) => r.title || "-"
    },
    {
      title: "启用",
      dataIndex: "enabled",
      width: 90,
      render: (_, r, index) => (
        <Switch
          checked={r.enabled !== false}
          onChange={(checked) => {
            setFloors((prev) =>
              prev.map((f, i) => (i === index ? { ...f, enabled: checked } : f))
            );
          }}
        />
      )
    },
    {
      title: "操作",
      valueType: "option",
      width: 160,
      render: (_, __, index) => (
        <Space>
          <Button
            type="link"
            disabled={index === 0}
            onClick={() => {
              setFloors((prev) => {
                const next = [...prev];
                [next[index - 1], next[index]] = [next[index], next[index - 1]];
                return next;
              });
            }}
          >
            上移
          </Button>
          <Button
            type="link"
            danger
            onClick={() => setFloors((prev) => prev.filter((_, i) => i !== index))}
          >
            删除
          </Button>
        </Space>
      )
    }
  ];

  return (
    <PageContainer
      header={{
        title: "店铺装修",
        subTitle: "模板 + 楼层可视化 · 无直播组件"
      }}
      extra={[
        <Button key="save" type="primary" onClick={() => saveDraft().catch((e) => message.error(e.message))}>
          保存草稿
        </Button>,
        <Button
          key="publish"
          onClick={async () => {
            try {
              await saveDraft();
              await apiFetch("/api/v1/seller/decoration/publish", { method: "POST" });
              message.success("已发布");
              await reload();
            } catch (err) {
              message.error(err instanceof Error ? err.message : "发布失败");
            }
          }}
        >
          发布上线
        </Button>
      ]}
    >
      {draft ? (
        <Space style={{ marginBottom: 16 }}>
          <Tag color="blue">草稿 #{draft.id}</Tag>
          <Tag>{draft.status}</Tag>
        </Space>
      ) : null}

      <ProCard title="基础设置" style={{ marginBottom: 16 }}>
        <ProForm
          submitter={false}
          initialValues={{ templateCode, themeColor }}
          onValuesChange={(_, all) => {
            if (all.templateCode) setTemplateCode(all.templateCode);
            if (all.themeColor) setThemeColor(all.themeColor);
          }}
        >
          <ProFormSelect
            name="templateCode"
            label="模板"
            options={templates.map((t) => ({
              label: `${t.name}（${t.code}）`,
              value: t.code
            }))}
            fieldProps={{
              onChange: (code: string) => {
                setTemplateCode(code);
                const tpl = templates.find((t) => t.code === code);
                if (tpl && !draft) setFloors(parseFloors(tpl.defaultFloorsJson));
              }
            }}
            rules={[{ required: true }]}
          />
          <ProFormText name="themeColor" label="主题色" rules={[{ required: true }]} />
        </ProForm>
      </ProCard>

      <ProCard
        title="楼层列表"
        extra={
          <Button
            type="dashed"
            onClick={() =>
              setFloors((prev) => [
                ...prev,
                {
                  type: FLOOR_TYPES[0],
                  sortOrder: (prev.length + 1) * 10,
                  enabled: true,
                  title: "新楼层"
                }
              ])
            }
          >
            添加楼层
          </Button>
        }
      >
        <ProTable<FloorItem>
          rowKey={(_, i) => String(i)}
          search={false}
          toolBarRender={false}
          pagination={false}
          dataSource={floors}
          columns={floorCols}
          locale={{ emptyText: "暂无楼层，请添加或选择模板" }}
        />
        <p style={{ marginTop: 12, color: "#888", fontSize: 13 }}>
          支持类型：{FLOOR_TYPES.join(" / ")} · 不含 LIVE（硬约束）
        </p>
      </ProCard>
    </PageContainer>
  );
}
