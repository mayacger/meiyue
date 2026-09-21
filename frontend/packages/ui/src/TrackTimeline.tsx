import type { CSSProperties } from "react";

/** 物流轨迹节点 */
export interface TrackNode {
  /** 状态码，如 IN_TRANSIT */
  status: string;
  /** 描述 */
  description: string;
  /** 时间 ISO 或可读字符串 */
  trackedAt?: string;
  /** 来源 MOCK / MANUAL 等 */
  source?: string;
}

export interface TrackTimelineProps {
  tracks: TrackNode[];
  /** 空态文案 */
  emptyText?: string;
}

const listStyle: CSSProperties = {
  listStyle: "none",
  margin: 0,
  padding: "0.25rem 0 0 0.75rem",
  borderLeft: "2px solid rgba(15, 61, 56, 0.2)"
};

const itemStyle: CSSProperties = {
  position: "relative",
  padding: "0 0 1rem 1rem"
};

const dotStyle: CSSProperties = {
  position: "absolute",
  left: "-0.55rem",
  top: "0.35rem",
  width: 10,
  height: 10,
  borderRadius: "50%",
  background: "#1A4D45",
  boxShadow: "0 0 0 3px rgba(26, 77, 69, 0.15)"
};

/**
 * 运单轨迹时间线（I24）
 * Buyer PC / Seller 共用；Taro 端用原生 View 仿照。
 */
export function TrackTimeline({ tracks, emptyText = "暂无轨迹节点" }: TrackTimelineProps) {
  if (!tracks || tracks.length === 0) {
    return <p style={{ color: "#5A6B66", margin: "0.5rem 0" }}>{emptyText}</p>;
  }
  // 新→旧展示：后端多为升序，这里倒序更符合时间线习惯
  const ordered = [...tracks].reverse();
  return (
    <ol className="my-track-timeline" style={listStyle}>
      {ordered.map((t, i) => (
        <li key={`${t.status}-${t.trackedAt}-${i}`} style={itemStyle}>
          <span style={dotStyle} aria-hidden />
          <strong style={{ display: "block", color: "#0F3D38" }}>{t.status}</strong>
          <span style={{ display: "block", fontSize: "0.9rem" }}>{t.description}</span>
          <span style={{ display: "block", fontSize: "0.8rem", color: "#5A6B66", marginTop: 2 }}>
            {t.trackedAt || ""}
            {t.source ? ` · ${t.source}` : ""}
          </span>
        </li>
      ))}
    </ol>
  );
}
