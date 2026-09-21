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
  SettingOutlined
} from "@ant-design/icons";
import { Dropdown } from "antd";
import { setToken } from "@meiyue/api";

/**
 * 商家后台 ProLayout
 * 菜单：概览 / 店铺设置 / 商品 / 库存 / 装修 / 发货 / 售后 / 评价 / 通知 / 结算 / 店券 / AI
 */
export function SellerLayout() {
  const location = useLocation();
  const navigate = useNavigate();

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
          routes: [
            { path: "/", name: "店铺概览", icon: <DashboardOutlined /> },
            { path: "/store", name: "店铺设置", icon: <SettingOutlined /> },
            { path: "/products", name: "商品管理", icon: <ShopOutlined /> },
            { path: "/inventory", name: "库存管理", icon: <DatabaseOutlined /> },
            { path: "/decoration", name: "店铺装修", icon: <SkinOutlined /> },
            { path: "/shipments", name: "发货履约", icon: <CarOutlined /> },
            { path: "/aftersales", name: "售后审核", icon: <CustomerServiceOutlined /> },
            { path: "/reviews", name: "评价管理", icon: <CommentOutlined /> },
            { path: "/tickets", name: "客服工单", icon: <CustomerServiceOutlined /> },
            { path: "/notifications", name: "站内通知", icon: <BellOutlined /> },
            { path: "/settlements", name: "结算账本", icon: <AccountBookOutlined /> },
            { path: "/coupons", name: "店券", icon: <GiftOutlined /> },
            { path: "/ai", name: "AI 素材", icon: <RobotOutlined /> }
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
          title: "商家",
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
