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

/** 楼层配置项（可视化编辑，非自由画布） */
interface FloorItem {
  type: string;
  sortOrder: number;
  enabled?: boolean;
  title?: string;
  imageUrl?: string;
  imageUrls?: string[];
  body?: string;
  html?: string;
  productIds?: number[];
  categoryIds?: number[];
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

/** 店铺装修可视化：楼层列表增删改排序 + 主题色；禁止直播 */
export function DecorationPage() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [templateCode, setTemplateCode] = useState("simple_banner");
  const [themeColor, setThemeColor] = useState("#1a5f4a");
  const [floors, setFloors] = useState<FloorItem[]>([]);
  const [msg, setMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [advanced, setAdvanced] = useState(false);

  useEffect(() => {
    apiFetch<Template[]>("/api/v1/decoration/templates").then((list) => {
      setTemplates(list);
      if (list[0]) {
        setTemplateCode(list[0].code);
        setFloors(parseFloors(list[0].defaultFloorsJson));
      }
    });
    apiFetch<StorePage>("/api/v1/seller/decoration/draft")
      .then((draft) => {
        setTemplateCode(draft.templateCode);
        setThemeColor(draft.themeColor);
        setFloors(parseFloors(draft.floorsJson));
      })
      .catch(() => undefined);
  }, []);

  function onTemplateChange(code: string) {
    setTemplateCode(code);
    const t = templates.find((x) => x.code === code);
    if (t) setFloors(parseFloors(t.defaultFloorsJson));
  }

  function moveFloor(index: number, dir: -1 | 1) {
    const next = [...floors];
    const j = index + dir;
    if (j < 0 || j >= next.length) return;
    [next[index], next[j]] = [next[j], next[index]];
    setFloors(next.map((f, i) => ({ ...f, sortOrder: (i + 1) * 10 })));
  }

  function updateFloor(index: number, patch: Partial<FloorItem>) {
    setFloors(floors.map((f, i) => (i === index ? { ...f, ...patch } : f)));
  }

  function removeFloor(index: number) {
    setFloors(floors.filter((_, i) => i !== index).map((f, i) => ({ ...f, sortOrder: (i + 1) * 10 })));
  }

  function addFloor(type: string) {
    if (type === "LIVE" || type === "LIVE_STREAM") {
      setError("不允许直播类楼层");
      return;
    }
    setFloors([
      ...floors,
      {
        type,
        sortOrder: (floors.length + 1) * 10,
        enabled: true,
        title: type
      }
    ]);
  }

  function floorsJson(): string {
    return JSON.stringify(floors.map((f, i) => ({ ...f, sortOrder: (i + 1) * 10 })));
  }

  async function save(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch("/api/v1/seller/decoration/draft", {
        method: "PUT",
        json: { templateCode, themeColor, floorsJson: floorsJson() }
      });
      setMsg("草稿已保存");
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    }
  }

  async function publish() {
    setError(null);
    try {
      await apiFetch("/api/v1/seller/decoration/draft", {
        method: "PUT",
        json: { templateCode, themeColor, floorsJson: floorsJson() }
      });
      await apiFetch("/api/v1/seller/decoration/publish", { method: "POST" });
      setMsg("已发布，买家可见");
    } catch (err) {
      setError(err instanceof Error ? err.message : "发布失败");
    }
  }

  return (
    <PageShell title="店铺装修" subtitle="楼层列表可视化编辑（非自由画布）；禁止直播（I11）">
      <p>
        <Link to="/">返回概览</Link> · <Link to="/products">商品管理</Link>
      </p>
      <form onSubmit={save} style={{ display: "grid", gap: 12, maxWidth: 720 }}>
        <label>
          模板{" "}
          <select value={templateCode} onChange={(e) => onTemplateChange(e.target.value)}>
            {templates.map((t) => (
              <option key={t.code} value={t.code}>{t.name}</option>
            ))}
          </select>
        </label>
        <label>
          主题色{" "}
          <input type="color" value={themeColor} onChange={(e) => setThemeColor(e.target.value)} />{" "}
          <input value={themeColor} onChange={(e) => setThemeColor(e.target.value)} style={{ width: 100 }} />
        </label>

        <section>
          <h3>楼层列表</h3>
          <p style={{ fontSize: 13, color: "#555" }}>
            允许：{FLOOR_TYPES.join(" / ")}。上移/下移调整顺序；禁止 LIVE。
          </p>
          {floors.map((f, i) => (
            <div key={i} style={{ borderTop: "1px solid #ddd", padding: "10px 0", display: "grid", gap: 6 }}>
              <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                <select value={f.type} onChange={(e) => updateFloor(i, { type: e.target.value })}>
                  {FLOOR_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                <label>
                  <input
                    type="checkbox"
                    checked={f.enabled !== false}
                    onChange={(e) => updateFloor(i, { enabled: e.target.checked })}
                  /> 启用
                </label>
                <span>#{f.sortOrder}</span>
                <button type="button" onClick={() => moveFloor(i, -1)}>上移</button>
                <button type="button" onClick={() => moveFloor(i, 1)}>下移</button>
                <button type="button" onClick={() => removeFloor(i)}>删除</button>
              </div>
              <input
                value={f.title || ""}
                onChange={(e) => updateFloor(i, { title: e.target.value })}
                placeholder="标题"
              />
              {(f.type === "BANNER" || f.type === "IMAGE_TEXT") && (
                <input
                  value={(f.imageUrl as string) || ""}
                  onChange={(e) => updateFloor(i, { imageUrl: e.target.value })}
                  placeholder="imageUrl"
                />
              )}
              {(f.type === "IMAGE_TEXT" || f.type === "RICH_TEXT") && (
                <textarea
                  rows={2}
                  value={(f.body as string) || (f.html as string) || ""}
                  onChange={(e) =>
                    updateFloor(i, f.type === "RICH_TEXT" ? { html: e.target.value } : { body: e.target.value })
                  }
                  placeholder={f.type === "RICH_TEXT" ? "html" : "body"}
                />
              )}
            </div>
          ))}
          <div style={{ marginTop: 8 }}>
            <select id="add-floor-type" defaultValue="BANNER">
              {FLOOR_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>{" "}
            <button
              type="button"
              onClick={() => {
                const el = document.getElementById("add-floor-type") as HTMLSelectElement;
                addFloor(el.value);
              }}
            >
              添加楼层
            </button>
          </div>
        </section>

        <label>
          <input type="checkbox" checked={advanced} onChange={(e) => setAdvanced(e.target.checked)} /> 高级：原始 JSON
        </label>
        {advanced ? (
          <textarea
            rows={8}
            value={floorsJson()}
            onChange={(e) => setFloors(parseFloors(e.target.value))}
            style={{ width: "100%", fontFamily: "monospace" }}
          />
        ) : null}

        {error ? <p style={{ color: "crimson" }}>{error}</p> : null}
        {msg ? <p>{msg}</p> : null}
        <div>
          <button type="submit">保存草稿</button>{" "}
          <button type="button" onClick={publish}>发布</button>
        </div>
      </form>
    </PageShell>
  );
}
