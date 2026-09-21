import type { UserConfigExport } from "@tarojs/cli";

/** 开发环境增量配置 */
export default {
  logger: { quiet: false, stats: true }
} satisfies UserConfigExport<"vite">;
