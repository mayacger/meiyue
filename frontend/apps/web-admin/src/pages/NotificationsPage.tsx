import {
  PageContainer,
  ProForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea
} from "@ant-design/pro-components";
import { App, Card, Typography } from "antd";
import { apiFetch } from "@meiyue/api";

/**
 * 站内通知发送（I16 ProForm 完善）
 * API：POST /admin/notifications
 * 字段：userId / audience / title / body / category
 */
export function NotificationsPage() {
  const { message } = App.useApp();

  return (
    <PageContainer header={{ title: "站内通知", subTitle: "写入买家 / 商家通知箱" }}>
      <Card>
        <Typography.Paragraph type="secondary">
          写入成功后买家端「通知」接口可拉取；推送通道（短信/微信）待密钥配置。
        </Typography.Paragraph>
        <ProForm
          onFinish={async (values) => {
            try {
              await apiFetch("/api/v1/admin/notifications", {
                method: "POST",
                json: {
                  userId: Number(values.userId),
                  audience: values.audience,
                  title: values.title,
                  body: values.body,
                  category: values.category
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
          <ProFormSelect
            name="audience"
            label="受众"
            initialValue="BUYER"
            options={[
              { label: "买家 BUYER", value: "BUYER" },
              { label: "商家 SELLER", value: "SELLER" }
            ]}
            rules={[{ required: true }]}
          />
          <ProFormSelect
            name="category"
            label="分类"
            initialValue="COUPON"
            options={[
              { label: "优惠券", value: "COUPON" },
              { label: "订单", value: "ORDER" },
              { label: "系统", value: "SYSTEM" }
            ]}
          />
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
