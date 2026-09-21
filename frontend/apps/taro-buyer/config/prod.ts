import type { UserConfigExport } from "@tarojs/cli";

/** 生产环境增量配置 */
export default {
  mini: {},
  h5: {}
} satisfies UserConfigExport<"vite">;
