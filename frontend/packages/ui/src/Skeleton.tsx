import type { CSSProperties } from "react";

/**
 * 轻量骨架屏（I29）
 * 用途：列表/卡片加载占位；全站复用，避免空白闪烁。
 *
 * @param rows   行数，默认 3
 * @param height 单行高度 px，默认 14
 */
export interface SkeletonProps {
  rows?: number;
  height?: number;
  style?: CSSProperties;
}

const bar: CSSProperties = {
  display: "block",
  width: "100%",
  borderRadius: 4,
  background: "linear-gradient(90deg, #e8eeec 0%, #f4f7f6 50%, #e8eeec 100%)",
  backgroundSize: "200% 100%",
  animation: "my-skel 1.2s ease-in-out infinite",
  marginBottom: 10
};

export function Skeleton({ rows = 3, height = 14, style }: SkeletonProps) {
  return (
    <div className="my-skeleton" style={style} aria-busy="true" aria-live="polite">
      <style>{`@keyframes my-skel{0%{background-position:200% 0}100%{background-position:-200% 0}}`}</style>
      {Array.from({ length: rows }).map((_, i) => (
        <span
          key={i}
          style={{
            ...bar,
            height,
            width: i === rows - 1 ? "62%" : "100%"
          }}
        />
      ))}
    </div>
  );
}
