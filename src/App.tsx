import { useState } from 'react'
import './App.css'

// 根组件：一个最小但可交互的示例页面
// 目的：为 Cloud Agent 开发环境提供一个真实可运行、可端到端验证的 React 应用
function App() {
  // 计数器状态，用于演示 React 的交互与状态更新
  const [count, setCount] = useState(0)

  return (
    <main className="app">
      <section className="card">
        <h1 className="title">Meiyue</h1>
        <p className="subtitle">React + Vite + TypeScript 开发环境已就绪</p>

        {/* 交互演示：点击按钮累加计数，验证前端运行时正常工作 */}
        <button
          className="counter-button"
          onClick={() => setCount((value) => value + 1)}
        >
          点击计数：{count}
        </button>

        <p className="hint">
          编辑 <code>src/App.tsx</code> 并保存，页面将自动热更新。
        </p>
      </section>
    </main>
  )
}

export default App
