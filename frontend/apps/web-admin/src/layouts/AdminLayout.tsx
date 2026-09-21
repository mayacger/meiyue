import { useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { ProLayout } from "@ant-design/pro-components";
import {
  AppstoreOutlined,
  AuditOutlined,
  BellOutlined,
  GiftOutlined,
  LogoutOutlined,
  TeamOutlined,
  DashboardOutlined,
  CustomerServiceOutlined,
  SettingOutlined,
  FileSearchOutlined
} from "@ant-design/icons";
import { Dropdown, Space, Tag } from "antd";
import { apiFetch, setToken } from "@meiyue/api";
import type { UserProfile } from "@meiyue/types";

/**
 * 平台后台 ProLayout（I21）
 * - 菜单：概览 / 入驻 / 券 / 运营配置 / 通知 / 工单 / 类目 / 账号
 */
export function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [me, setMe] = useState<UserProfile | null>(null);

  useEffect(() => {
    apiFetch<UserProfile>("/api/v1/auth/me")
      .then(setMe)
      .catch(() => setMe(null));
  }, []);

  return (
    <div style={{ height: "100vh" }}>
      <ProLayout
        title="美月商城"
        logo={false}
        layout="mix"
        location={{ pathname: location.pathname }}
        token={{
          header: { colorBgHeader: "#0F3D38", colorHeaderTitle: "#F7F6F3" },
          sider: { colorMenuBackground: "#fff" }
        }}
        route={{
          path: "/",
          routes: [
            { path: "/dashboard", name: "运营概览", icon: <DashboardOutlined /> },
            { path: "/onboarding", name: "入驻审核", icon: <AuditOutlined /> },
            { path: "/coupons", name: "平台券", icon: <GiftOutlined /> },
            { path: "/platform-config", name: "运营配置", icon: <SettingOutlined /> },
            { path: "/audit-logs", name: "审计日志", icon: <FileSearchOutlined /> },
            { path: "/notifications", name: "站内通知", icon: <BellOutlined /> },
            { path: "/tickets", name: "客服工单", icon: <CustomerServiceOutlined /> },
            { path: "/categories", name: "类目管理", icon: <AppstoreOutlined /> },
            { path: "/account", name: "账号与权限", icon: <TeamOutlined /> }
          ]
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
          title: me?.displayName || me?.username || "平台管理员",
          render: (_props, dom) => (
            <Space>
              {me?.roles?.map((r) => (
                <Tag key={r} color="green">
                  {r}
                </Tag>
              ))}
              <Dropdown
                menu={{
                  items: [
                    {
                      key: "account",
                      label: "账号与权限",
                      onClick: () => navigate("/account")
                    },
                    {
                      key: "logout",
                      icon: <LogoutOutlined />,
                      label: "退出登录",
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
            </Space>
          )
        }}
      >
        <Outlet />
      </ProLayout>
    </div>
  );
}
