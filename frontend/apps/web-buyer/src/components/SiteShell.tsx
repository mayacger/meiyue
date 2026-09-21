import { FormEvent, useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { getToken, setToken } from "@meiyue/api";
import "./SiteShell.css";

/**
 * 站点壳：顶栏品牌 + 主导航 + Outlet
 * 首屏品牌名「美月商城」为 hero 级信号（衬线）
 */
export function SiteShell() {
  const navigate = useNavigate();
  const loggedIn = !!getToken();
  const [open, setOpen] = useState(false);

  function logout(e: FormEvent) {
    e.preventDefault();
    setToken(null);
    navigate("/");
  }

  return (
    <div className="my-shell">
      <header className="my-header">
        <div className="my-header__inner">
          <Link to="/" className="my-brand" aria-label="美月商城首页">
            <span className="my-brand__mark">美月商城</span>
            <span className="my-brand__en">meiyuemall</span>
          </Link>
          <button
            type="button"
            className="my-nav-toggle"
            aria-label="菜单"
            onClick={() => setOpen((v) => !v)}
          >
            菜单
          </button>
          <nav className={`my-nav ${open ? "is-open" : ""}`}>
            <NavLink to="/" end onClick={() => setOpen(false)}>
              首页
            </NavLink>
            <NavLink to="/products" onClick={() => setOpen(false)}>
              全部商品
            </NavLink>
            <NavLink to="/coupons" onClick={() => setOpen(false)}>
              领券
            </NavLink>
            <NavLink to="/favorites" onClick={() => setOpen(false)}>
              收藏
            </NavLink>
            <NavLink to="/cart" onClick={() => setOpen(false)}>
              购物车
            </NavLink>
            <NavLink to="/orders" onClick={() => setOpen(false)}>
              订单
            </NavLink>
            <NavLink to="/addresses" onClick={() => setOpen(false)}>
              地址
            </NavLink>
            <NavLink to="/notifications" onClick={() => setOpen(false)}>
              通知
            </NavLink>
            <NavLink to="/tickets" onClick={() => setOpen(false)}>
              工单
            </NavLink>
            <NavLink to="/aftersales" onClick={() => setOpen(false)}>
              售后
            </NavLink>
            {loggedIn ? (
              <a href="#logout" onClick={logout}>
                退出
              </a>
            ) : (
              <NavLink to="/login" className="my-nav__cta" onClick={() => setOpen(false)}>
                登录
              </NavLink>
            )}
          </nav>
        </div>
      </header>
      <main className="my-main">
        <Outlet />
      </main>
      <footer className="my-footer">
        <div className="my-footer__inner">
          <p className="my-brand__mark">美月商城</p>
          <p>多商家好物 · 完整履约 · 不做直播</p>
        </div>
      </footer>
    </div>
  );
}
