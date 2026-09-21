import { useRef } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ModalForm, PageContainer, ProFormText, ProTable } from "@ant-design/pro-components";
import { App, Button, Popconfirm, Space, Tag } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 商家员工管理（I26 · 仅店主）
 *
 * 入口：SellerLayout → /staff
 * API：
 *   - GET  /api/v1/seller/staff           本店成员列表
 *   - POST /api/v1/seller/staff/invite    邀请已注册用户为 STAFF
 *   - DELETE /api/v1/seller/staff/{id}    移除店员（不可移除 OWNER）
 *
 * 关系：SellerMember(OWNER/STAFF) + UserRole SELLER_STAFF
 * 权限：结算 / 店铺设置写 / 本页仅 SELLER_OWNER
 */

/** 员工行：id / userId / username / displayName / memberRole / createdAt */
interface StaffMember {
  id: number;
  userId: number;
  username: string | null;
  displayName: string | null;
  memberRole: "OWNER" | "STAFF" | string;
  createdAt: string | null;
}

export function StaffPage() {
  const actionRef = useRef<ActionType>();
  const { message } = App.useApp();

  const columns: ProColumns<StaffMember>[] = [
    { title: "成员 ID", dataIndex: "id", width: 88 },
    { title: "用户 ID", dataIndex: "userId", width: 88 },
    { title: "登录名", dataIndex: "username", ellipsis: true },
    { title: "展示名", dataIndex: "displayName", ellipsis: true },
    {
      title: "角色",
      dataIndex: "memberRole",
      width: 100,
      render: (_, r) => (
        <Tag color={r.memberRole === "OWNER" ? "gold" : "blue"}>
          {r.memberRole === "OWNER" ? "店主" : "店员"}
        </Tag>
      )
    },
    { title: "加入时间", dataIndex: "createdAt", width: 200, ellipsis: true },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, r) =>
        r.memberRole === "OWNER" ? (
          <span style={{ color: "#999" }}>—</span>
        ) : (
          <Popconfirm
            title={`确认移除店员 ${r.username || r.userId}？`}
            onConfirm={async () => {
              try {
                await apiFetch(`/api/v1/seller/staff/${r.id}`, { method: "DELETE" });
                message.success("已移除");
                actionRef.current?.reload();
              } catch (err) {
                message.error(err instanceof Error ? err.message : "移除失败");
              }
            }}
          >
            <Button type="link" danger>
              移除
            </Button>
          </Popconfirm>
        )
    }
  ];

  return (
    <PageContainer
      header={{
        title: "员工管理",
        subTitle: "店主邀请已注册用户 · 店员可管商品/发货，结算与店铺设置仅店主"
      }}
    >
      <ProTable<StaffMember>
        actionRef={actionRef}
        rowKey="id"
        search={false}
        columns={columns}
        toolBarRender={() => [
          <ModalForm
            key="invite"
            title="邀请店员"
            trigger={<Button type="primary">邀请店员</Button>}
            modalProps={{ destroyOnClose: true }}
            onFinish={async (values) => {
              try {
                await apiFetch("/api/v1/seller/staff/invite", {
                  method: "POST",
                  json: { username: String(values.username).trim() }
                });
                message.success("已邀请");
                actionRef.current?.reload();
                return true;
              } catch (err) {
                message.error(err instanceof Error ? err.message : "邀请失败");
                return false;
              }
            }}
          >
            <ProFormText
              name="username"
              label="用户登录名"
              placeholder="对方需先注册账号"
              rules={[{ required: true, message: "请输入登录名" }]}
              extra="邀请后对方获得 SELLER_STAFF，可登录商家后台处理商品与发货"
            />
          </ModalForm>
        ]}
        request={async () => {
          const data = await apiFetch<StaffMember[]>("/api/v1/seller/staff");
          return { data, success: true, total: data.length };
        }}
      />
      <Space style={{ marginTop: 8 }}>
        <span style={{ color: "#666", fontSize: 13 }}>
          提示：不可邀请已归属其他店铺的用户；不可移除店主本人。
        </span>
      </Space>
    </PageContainer>
  );
}
