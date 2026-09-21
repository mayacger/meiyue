import { useEffect, useState } from "react";
import {
  PageContainer,
  ProForm,
  ProFormSelect,
  ProFormText
} from "@ant-design/pro-components";
import { App, Card, Space, Tag } from "antd";
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

/**
 * 店铺装修骨架（模板 + 主题色 + 草稿/发布）
 * 禁止直播组件；深度可视化编辑下轮补齐
 */
export function DecorationPage() {
  const { message } = App.useApp();
  const [templates, setTemplates] = useState<Template[]>([]);
  const [draft, setDraft] = useState<StorePage | null>(null);

  async function reload() {
    const list = await apiFetch<Template[]>("/api/v1/decoration/templates");
    setTemplates(list);
    try {
      setDraft(await apiFetch<StorePage>("/api/v1/seller/decoration/draft"));
    } catch {
      setDraft(null);
    }
  }

  useEffect(() => {
    reload().catch((e) => message.error(e instanceof Error ? e.message : "加载失败"));
  }, [message]);

  return (
    <PageContainer header={{ title: "店铺装修", subTitle: "模板 + 楼层 + 主题色（无直播）" }}>
      {draft ? (
        <Space style={{ marginBottom: 16 }}>
          <Tag color="blue">草稿模板 {draft.templateCode}</Tag>
          <Tag>主题色 {draft.themeColor}</Tag>
          <Tag>{draft.status}</Tag>
        </Space>
      ) : null}
      <Card>
        <ProForm
          initialValues={{
            templateCode: draft?.templateCode || templates[0]?.code,
            themeColor: draft?.themeColor || "#1A4D45"
          }}
          onFinish={async (values) => {
            try {
              const tpl = templates.find((t) => t.code === values.templateCode);
              await apiFetch("/api/v1/seller/decoration/draft", {
                method: "PUT",
                json: {
                  templateCode: values.templateCode,
                  themeColor: values.themeColor,
                  floorsJson: draft?.floorsJson || tpl?.defaultFloorsJson || "[]"
                }
              });
              message.success("草稿已保存");
              await reload();
              return true;
            } catch (err) {
              message.error(err instanceof Error ? err.message : "保存失败");
              return false;
            }
          }}
          submitter={{
            searchConfig: { submitText: "保存草稿" },
            render: (_props, doms) => [
              ...doms,
              <a
                key="publish"
                style={{ marginLeft: 12 }}
                onClick={async () => {
                  try {
                    await apiFetch("/api/v1/seller/decoration/publish", { method: "POST" });
                    message.success("已发布");
                    await reload();
                  } catch (err) {
                    message.error(err instanceof Error ? err.message : "发布失败");
                  }
                }}
              >
                发布上线
              </a>
            ]
          }}
        >
          <ProFormSelect
            name="templateCode"
            label="模板"
            options={templates.map((t) => ({
              label: `${t.name}（${t.code}）`,
              value: t.code
            }))}
            rules={[{ required: true }]}
          />
          <ProFormText name="themeColor" label="主题色" rules={[{ required: true }]} />
        </ProForm>
      </Card>
    </PageContainer>
  );
}
