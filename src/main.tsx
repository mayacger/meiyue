import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './index.css'

// 应用入口：将根组件 <App /> 挂载到 index.html 中 id 为 root 的节点上
const rootElement = document.getElementById('root')

if (!rootElement) {
  // 防御式判断：理论上 index.html 一定存在该节点，此处仅用于类型收窄与更清晰的报错
  throw new Error('未找到 id 为 "root" 的挂载节点')
}

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
