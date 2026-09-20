# 美月商城 · 备份与恢复演练（I7）

> 目标：证明 PostgreSQL 业务库可备份、可恢复；本环境可手工演练，CI 不强制自动跑。

## 入口与范围

| 项 | 说明 |
|----|------|
| 库 | `meiyue_mall`（应用数据源） |
| 不含 | Redis（当前 exclude）、对象存储（I8 本地 MOCK URL） |
| 密钥 | JWT / 支付 / AI 密钥仅在环境变量或配置，**不在库内明文**，恢复后需重新注入 |

## 备份（逻辑备份）

```bash
# 自定义格式，便于并行恢复
pg_dump -h 127.0.0.1 -U meiyue -d meiyue_mall -Fc -f /tmp/meiyue_mall_$(date +%Y%m%d).dump

# 或纯 SQL（便于审阅）
pg_dump -h 127.0.0.1 -U meiyue -d meiyue_mall -f /tmp/meiyue_mall_$(date +%Y%m%d).sql
```

建议保留最近 N 份，并异地拷贝（生产再定 RPO）。

## 恢复演练（建议在旁路库）

```bash
# 1. 创建演练库
createdb -h 127.0.0.1 -U meiyue meiyue_mall_drill

# 2. 恢复
pg_restore -h 127.0.0.1 -U meiyue -d meiyue_mall_drill --clean --if-exists /tmp/meiyue_mall_YYYYMMDD.dump
# 或：psql -h 127.0.0.1 -U meiyue -d meiyue_mall_drill -f /tmp/meiyue_mall_YYYYMMDD.sql

# 3. 抽检
psql -h 127.0.0.1 -U meiyue -d meiyue_mall_drill -c "SELECT COUNT(*) FROM flyway_schema_history;"
psql -h 127.0.0.1 -U meiyue -d meiyue_mall_drill -c "SELECT COUNT(*) FROM orders;"
psql -h 127.0.0.1 -U meiyue -d meiyue_mall_drill -c "SELECT COUNT(*) FROM audit_logs;"

# 4. 清理演练库
dropdb -h 127.0.0.1 -U meiyue meiyue_mall_drill
```

## 验收标准

1. 备份文件非空且 `pg_restore`/`psql` 无致命错误  
2. `flyway_schema_history` 版本与生产一致（含 V7/V8）  
3. 抽检订单/支付/售后行数与备份前快照一致（允许演练窗口增量说明）  
4. 恢复后应用若切库启动，需重新配置密钥环境变量  

## 关系图

```
应用写库 ──定期──► pg_dump ──异地──► 备份介质
                      │
                 演练恢复 ▼
              meiyue_mall_drill ──抽检──► 通过/失败记录
```
