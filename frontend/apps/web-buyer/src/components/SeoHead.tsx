import { useEffect } from "react";

/**
 * 买家 PC SEO 基础（I28）
 * 路由级设置 document.title / meta description / Open Graph。
 * 无额外依赖；商品详情可传 ogImage。
 *
 * @param title       页面标题（会拼「 · 美月商城」）
 * @param description meta description / og:description
 * @param ogImage     og:image（商品封面等）
 * @param path        规范路径，默认 location.pathname
 */

export interface SeoHeadProps {
  title: string;
  description?: string;
  ogImage?: string | null;
  path?: string;
}

function upsertMeta(attr: "name" | "property", key: string, content: string) {
  let el = document.head.querySelector(`meta[${attr}="${key}"]`) as HTMLMetaElement | null;
  if (!el) {
    el = document.createElement("meta");
    el.setAttribute(attr, key);
    document.head.appendChild(el);
  }
  el.content = content;
}

export function SeoHead({ title, description, ogImage, path }: SeoHeadProps) {
  useEffect(() => {
    const fullTitle = title.includes("美月") ? title : `${title} · 美月商城`;
    document.title = fullTitle;
    const desc =
      description ||
      "美月商城 — 多商家精选上架，完整履约与售后。不做直播带货。";
    upsertMeta("name", "description", desc);
    upsertMeta("property", "og:title", fullTitle);
    upsertMeta("property", "og:description", desc);
    upsertMeta("property", "og:type", ogImage ? "product" : "website");
    const url = `${window.location.origin}${path ?? window.location.pathname}`;
    upsertMeta("property", "og:url", url);
    if (ogImage) {
      upsertMeta("property", "og:image", ogImage);
    }
  }, [title, description, ogImage, path]);

  return null;
}
