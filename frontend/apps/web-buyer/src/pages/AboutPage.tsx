import { Link } from "react-router-dom";
import "./StaticPage.css";

/**
 * I37：关于美月（静态）
 * 入口：页脚 · /about
 */
export function AboutPage() {
  return (
    <article className="my-static my-page my-fade-up">
      <h1 className="my-page-title">关于美月商城</h1>
      <p className="my-page-lead">多商家好物 · 完整履约 · 不做直播分账演示</p>
      <p>
        美月商城是面向中国大陆的多租户 B2B2C 电商演示平台：买家浏览下单、商家经营履约、平台审核配置。
        支付与 AI 默认 MOCK，生产密钥见运维文档。
      </p>
      <p>
        <Link to="/help">帮助中心</Link> · <Link to="/">返回首页</Link>
      </p>
    </article>
  );
}
