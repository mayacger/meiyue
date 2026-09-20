import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "@meiyue/api";
import { PageShell } from "@meiyue/ui";

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

/** 店铺装修：选模板、主题色、保存草稿、发布（无直播） */
export function DecorationPage() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [templateCode, setTemplateCode] = useState("simple_banner");
  const [themeColor, setThemeColor] = useState("#1a5f4a");
  const [floorsJson, setFloorsJson] = useState("[]");
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Template[]>("/api/v1/decoration/templates").then((list) => {
      setTemplates(list);
      if (list[0]) {
        setTemplateCode(list[0].code);
        setFloorsJson(list[0].defaultFloorsJson);
      }
    });
    apiFetch<StorePage>("/api/v1/seller/decoration/draft")
      .then((draft) => {
        setTemplateCode(draft.templateCode);
        setThemeColor(draft.themeColor);
        setFloorsJson(draft.floorsJson);
      })
      .catch(() => undefined);
  }, []);

  function onTemplateChange(code: string) {
    setTemplateCode(code);
    const t = templates.find((x) => x.code === code);
    if (t) {
      setFloorsJson(t.defaultFloorsJson);
    }
  }

  async function save(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/decoration/draft", {
        method: "PUT",
        json: { templateCode, themeColor, floorsJson }
      });
      setMsg("草稿已保存");
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    }
  }

  async function publish() {
    setError(null);
    try {
      await apiFetch("/api/v1/seller/decoration/publish", { method: "POST" });
      setMsg("已发布，买家可见");
    } catch (err) {
      setError(err instanceof Error ? err.message : "发布失败");
    }
  }

  return (
    <PageShell title="店铺装修" subtitle="模板 + 楼层配置 + 主题色；无直播组件（I2）">
      <p>
        <Link to="/">返回概览</Link> · <Link to="/products">商品管理</Link>
      </p>
      <form onSubmit={save} style={{ display: "grid", gap: 8, maxWidth: 640 }}>
        <label>
          模板
          <select value={templateCode} onChange={(e) => onTemplateChange(e.target.value)}>
            {templates.map((t) => (
              <option key={t.code} value={t.code}>
                {t.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          主题色
          <input value={themeColor} onChange={(e) => setThemeColor(e.target.value)} />
        </label>
        <label>
          楼层 JSON（BANNER / PRODUCT_RECOMMEND / IMAGE_TEXT / PRODUCT_GROUP）
          <textarea rows={10} value={floorsJson} onChange={(e) => setFloorsJson(e.target.value)} style={{ width: "100%", fontFamily: "monospace" }} />
        </label>
        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        {msg ? <p>{msg}</p> : null}
        <div>
          <button type="submit">保存草稿</button>{" "}
          <button type="button" onClick={publish}>
            发布
          </button>
        </div>
      </form>
    </PageShell>
  );
}
