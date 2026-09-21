import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { ProLayout } from "@ant-design/pro-components";
import {
  AuditOutlined,
  BellOutlined,
  GiftOutlined,
  LogoutOutlined
} from "@ant-design/icons";
import { Dropdown } from "antd";
import { setToken } from "@meiyue/api";

/**
 * 平台后台 ProLayout
 * - 侧栏：入驻审核 / 平台券 / 通知
 * - 右上角退出清 Token
 */
export function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();

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
            {
              path: "/onboarding",
              name: "入驻审核",
              icon: <AuditOutlined />
            },
            {
              path: "/coupons",
              name: "平台券",
              icon: <GiftOutlined />
            },
            {
              path: "/notifications",
              name: "站内通知",
              icon: <BellOutlined />
            }
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
          title: "平台管理员",
          render: (_props, dom) => (
            <Dropdown
              menu={{
                items: [
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
          )
        }}
      >
        <Outlet />
      </ProLayout>
    </div>
  );
}
