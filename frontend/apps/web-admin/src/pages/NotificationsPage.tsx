import {
  PageContainer,
  ProForm,
  ProFormDigit,
  ProFormText,
  ProFormTextArea
} from "@ant-design/pro-components";
import { App, Card } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 站内通知发送（骨架）
 * API：POST /admin/notifications
 * 字段：userId / audience / title / body / category
 */
export function NotificationsPage() {
  const { message } = App.useApp();

  return (
    <PageContainer header={{ title: "站内通知", subTitle: "写入买家通知箱" }}>
      <Card>
        <ProForm
          onFinish={async (values) => {
            try {
              await apiFetch("/api/v1/admin/notifications", {
                method: "POST",
                json: {
                  userId: Number(values.userId),
                  audience: "BUYER",
                  title: values.title,
                  body: values.body,
                  category: "COUPON"
                }
              });
              message.success("通知已写入");
              return true;
            } catch (err) {
              message.error(err instanceof Error ? err.message : "发送失败");
              return false;
            }
          }}
        >
          <ProFormDigit name="userId" label="目标用户 ID" initialValue={3} rules={[{ required: true }]} />
          <ProFormText name="title" label="标题" initialValue="平台活动" rules={[{ required: true }]} />
          <ProFormTextArea
            name="body"
            label="正文"
            initialValue="您有一张平台券可领"
            rules={[{ required: true }]}
          />
        </ProForm>
      </Card>
    </PageContainer>
  );
}
