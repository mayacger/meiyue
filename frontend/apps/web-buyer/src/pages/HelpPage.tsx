import { Link } from "react-router-dom";
import "./StaticPage.css";

/**
 * I37：帮助中心（静态 FAQ）
 * 入口：页脚 · /help
 */
export function HelpPage() {
  return (
    <article className="my-static my-page my-fade-up">
      <h1 className="my-page-title">帮助中心</h1>
      <p className="my-page-lead">常见问题 · 演示环境说明</p>
      <section>
        <h2>如何下单？</h2>
        <p>登录买家账号 → 加购 → 结算。演示支付为 MOCK，不会真实扣款。</p>
      </section>
      <section>
        <h2>售后怎么申请？</h2>
        <p>订单详情或「售后」入口提交；商家审核后走 MOCK 退款通道。</p>
      </section>
      <section>
        <h2>无障碍</h2>
        <p>页面支持键盘 Tab 焦点环；主要按钮带 aria-label。详见站点页脚链接。</p>
      </section>
      <p>
        <Link to="/about">关于美月</Link> · <Link to="/">返回首页</Link>
      </p>
    </article>
  );
}
