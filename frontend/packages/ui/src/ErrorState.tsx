import type { CSSProperties, ReactNode } from "react";

/**
 * 统一错误态（I23）
 * 用途：接口失败 / 权限不足等；不展示堆栈。
 *
 * @param message 错误说明
 * @param children 可选重试按钮等
 */
export interface ErrorStateProps {
  message: string;
  children?: ReactNode;
  style?: CSSProperties;
}

const wrap: CSSProperties = {
  padding: "1.25rem 1rem",
  borderRadius: 8,
  background: "rgba(180, 60, 50, 0.08)",
  color: "#8B2E26",
  fontSize: "0.95rem"
};

export function ErrorState({ message, children, style }: ErrorStateProps) {
  return (
    <div className="my-error-state" style={{ ...wrap, ...style }} role="alert">
      <p style={{ margin: "0 0 0.5rem" }}>{message}</p>
      {children}
    </div>
  );
}
