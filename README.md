# Meiyue

一个基于 **React + Vite + TypeScript** 的最小可运行前端工程，同时作为 Cursor Cloud Agent 开发环境的验证载体。

> 说明：仓库初始为空（仅有占位文件）。为了让 Cloud Agent 开发环境能够“端到端”地安装依赖、启动应用并验证运行，这里搭建了一个最小但真实可运行的 React 应用作为基座。后续业务代码可直接在此基础上迭代。

---

## 目录结构

```text
.
├── .cursor/
│   └── environment.json     # Cloud Agent 环境配置（安装命令 + 开发服务器终端）
├── public/
│   └── vite.svg             # 静态资源（页面 favicon）
├── src/
│   ├── App.tsx              # 根组件（含计数器交互示例）
│   ├── App.css              # 根组件样式
│   ├── main.tsx             # 应用入口，挂载 <App /> 到 #root
│   ├── index.css            # 全局基础样式
│   └── vite-env.d.ts        # Vite 客户端类型声明
├── index.html               # HTML 模板，包含挂载节点 #root
├── vite.config.ts           # Vite 配置（React 插件、dev/preview 端口）
├── eslint.config.js         # ESLint 扁平配置
├── tsconfig.json            # TS 根配置（引用 app / node 两个子配置）
├── tsconfig.app.json        # 应用源码 TS 配置
├── tsconfig.node.json       # 构建工具链 TS 配置
├── package.json             # 依赖与脚本，pnpm 包管理器锁定
└── pnpm-lock.yaml           # pnpm 锁文件（保证可复现安装）
```

## 应用入口与渲染关系

```text
index.html (#root)
      │  加载 <script src="/src/main.tsx">
      ▼
src/main.tsx  ── createRoot(#root).render(<App/>)
      │
      ▼
src/App.tsx   ── 页面 UI + useState 计数器交互
      │
      ├── import './App.css'    页面样式
      └── (main.tsx) import './index.css'  全局样式
```

## 环境要求

- Node.js `>= 20`（当前验证使用 `v22`）
- 包管理器 **pnpm**（版本由 `package.json` 的 `packageManager` 字段锁定为 `pnpm@10.33.3`，可通过 `corepack enable` 自动启用）

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `pnpm install` | 安装依赖（CI/环境中使用 `pnpm install --frozen-lockfile` 保证可复现） |
| `pnpm dev` | 启动开发服务器（默认 http://localhost:5173 ，支持热更新 HMR） |
| `pnpm build` | 类型检查 + 生产构建，产物输出到 `dist/` |
| `pnpm preview` | 本地预览生产构建（默认 http://localhost:4173 ） |
| `pnpm lint` | 运行 ESLint 静态检查 |
| `pnpm typecheck` | 仅做 TypeScript 类型检查，不产出文件 |

## 本地快速开始

```bash
corepack enable          # 启用 pnpm（若尚未启用）
pnpm install             # 安装依赖
pnpm dev                 # 启动开发服务器，浏览器打开 http://localhost:5173
```

## Cloud Agent 环境说明

环境配置位于 [`.cursor/environment.json`](.cursor/environment.json)：

- `install`：`corepack enable && pnpm install --frozen-lockfile`
  - 在检出源码后安装依赖，使用锁文件保证可复现，且可幂等重复执行。
- `terminals`：名为 `vite-dev` 的常驻终端，运行 `pnpm dev` 启动开发服务器，便于在 Agent 中查看日志与热更新。

生产构建（`pnpm build`）不放入 `install`，以免每次启动都触发构建；如需预览产物可手动运行 `pnpm preview`。
