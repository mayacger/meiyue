/// <reference types="@tarojs/taro" />

declare module "*.css";
declare module "*.png";
declare module "*.jpg";

/** Taro 宏：页面/应用配置 */
declare function defineAppConfig(config: Taro.Config): Taro.Config;
declare function definePageConfig(config: Taro.Config): Taro.Config;
