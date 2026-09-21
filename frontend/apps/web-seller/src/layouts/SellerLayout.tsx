import { useEffect, useMemo, useState, type ReactNode } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { ProLayout } from "@ant-design/pro-components";
import {
  CarOutlined,
  CustomerServiceOutlined,
  DashboardOutlined,
  GiftOutlined,
  LogoutOutlined,
  RobotOutlined,
  ShopOutlined,
  SkinOutlined,
  AccountBookOutlined,
  CommentOutlined,
  BellOutlined,
  DatabaseOutlined,
  SettingOutlined,
  UserOutlined,
  TeamOutlined
} from "@ant-design/icons";
import { Dropdown } from "antd";
import { apiFetch, setToken } from "@meiyue/api";
import type { UserProfile } from "@meiyue/types";

/**
 * 商家后台 ProLayout（I26：按 OWNER/STAFF 隐藏菜单）
 *
 * OWNER 独有：店铺设置写、结算账本、员工管理
 * STAFF 可用：概览 / 商品 / 库存 / 发货 / 售后等
 */

type MenuRoute = {
  path: string;
  name: string;
  icon: ReactNode;
  /** 仅店主可见 */
  ownerOnly?: boolean;
};

const ALL_ROUTES: MenuRoute[] = [
  { path: "/", name: "店铺概览", icon: <DashboardOutlined /> },
  { path: "/store", name: "店铺设置", icon: <SettingOutlined />, ownerOnly: true },
  { path: "/staff", name: "员工管理", icon: <TeamOutlined />, ownerOnly: true },
  { path: "/settings", name: "账号设置", icon: <UserOutlined /> },
  { path: "/products", name: "商品管理", icon: <ShopOutlined /> },
  { path: "/inventory", name: "库存管理", icon: <DatabaseOutlined /> },
  { path: "/decoration", name: "店铺装修", icon: <SkinOutlined /> },
  { path: "/shipments", name: "发货履约", icon: <CarOutlined /> },
  { path: "/aftersales", name: "售后审核", icon: <CustomerServiceOutlined /> },
  { path: "/reviews", name: "评价管理", icon: <CommentOutlined /> },
  { path: "/tickets", name: "客服工单", icon: <CustomerServiceOutlined /> },
  { path: "/notifications", name: "站内通知", icon: <BellOutlined /> },
  { path: "/settlements", name: "结算账本", icon: <AccountBookOutlined />, ownerOnly: true },
  { path: "/coupons", name: "店券", icon: <GiftOutlined /> },
  { path: "/ai", name: "AI 素材", icon: <RobotOutlined /> }
];

export function SellerLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [me, setMe] = useState<UserProfile | null>(null);

  useEffect(() => {
    apiFetch<UserProfile>("/api/v1/auth/me")
      .then(setMe)
      .catch(() => setMe(null));
  }, []);

  /** 是否店主：JWT roles 含 SELLER_OWNER */
  const isOwner = Boolean(me?.roles?.includes("SELLER_OWNER"));

  const routes = useMemo(
    () => ALL_ROUTES.filter((r) => !r.ownerOnly || isOwner),
    [isOwner]
  );

  return (
    <div style={{ height: "100vh" }}>
      <ProLayout
        title="美月商家"
        logo={false}
        layout="mix"
        location={{ pathname: location.pathname }}
        token={{
          header: { colorBgHeader: "#0F3D38", colorHeaderTitle: "#F7F6F3" }
        }}
        route={{
          path: "/",
          routes
        }}
        menuItemRender={(item, dom) => (
          <a
            onClick={(e) => {
              e.preventDefault();
              if (item.path) navigate(item.path);
            }}
          >
            {dom}
          </a>
        )}
        avatarProps={{
          title: me?.displayName || me?.username || "商家",
          render: (_p, dom) => (
            <Dropdown
              menu={{
                items: [
                  {
                    key: "logout",
                    icon: <LogoutOutlined />,
                    label: "退出",
                    onClick: () => {
                      setToken(null);
                      navigate("/login");
                    }
                  }
                ]
              }}
            >
              {dom}
            </Dropdown>
          )
        }}
      >
        <Outlet />
      </ProLayout>
    </div>
  );
}
