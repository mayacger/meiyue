import type { ReactNode } from "react";

/**
 * 页面壳：三端共用的最小布局占位。
 * 字段：
 * - title：页面主标题
 * - subtitle：一句说明
 * - children：正文
 */
export interface PageShellProps {
  title: string;
  subtitle?: string;
  children?: ReactNode;
}

/**
 * 极简页面壳（非设计定稿；I1 起各端换正式布局）。
 */
export function PageShell({ title, subtitle, children }: PageShellProps) {
  return (
    <div style={{ fontFamily: "system-ui, sans-serif", padding: "2rem", maxWidth: 720 }}>
      <p style={{ letterSpacing: "0.08em", textTransform: "uppercase", color: "#666", margin: 0 }}>
        美月商城
      </p>
      <h1 style={{ marginTop: "0.5rem" }}>{title}</h1>
      {subtitle ? <p style={{ color: "#444" }}>{subtitle}</p> : null}
      {children}
    </div>
  );
}
