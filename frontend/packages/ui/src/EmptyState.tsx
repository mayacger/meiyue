import type { CSSProperties, ReactNode } from "react";

/**
 * 统一空态（I23）
 * 用途：列表/收藏/订单等无数据时展示；可附带操作区 children。
 *
 * @param title   主文案，默认「暂无内容」
 * @param hint    次要说明
 * @param children 可选 CTA（如 Link / Button）
 */
export interface EmptyStateProps {
  title?: string;
  hint?: string;
  children?: ReactNode;
  style?: CSSProperties;
}

const wrap: CSSProperties = {
  padding: "2.5rem 1rem",
  textAlign: "center",
  color: "#5A6B66"
};

const titleStyle: CSSProperties = {
  margin: "0 0 0.35rem",
  fontSize: "1.05rem",
  fontWeight: 500,
  color: "#1A4D45"
};

const hintStyle: CSSProperties = {
  margin: "0 0 1rem",
  fontSize: "0.9rem"
};

export function EmptyState({ title = "暂无内容", hint, children, style }: EmptyStateProps) {
  return (
    <div className="my-empty-state" style={{ ...wrap, ...style }} role="status">
      <p style={titleStyle}>{title}</p>
      {hint ? <p style={hintStyle}>{hint}</p> : null}
      {children}
    </div>
  );
}
