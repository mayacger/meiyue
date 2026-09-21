# 美月商城后端说明
#
# 入口
#   - API：meiyue-boot → com.meiyuemall.boot.MeiyueBootApplication
#   - Worker：meiyue-worker → com.meiyuemall.worker.MeiyueWorkerApplication
#
# 模块关系（简图）
#
#   meiyue-boot ──组装──► identity / tenant / catalog / decoration /
#                         trade / payment / logistics / aftersale /
#                         ai-assist / support / platform
#        │
#        └── depends ► meiyue-common（TenantContext、Security、ErrorCode）
#
#   meiyue-worker ──独立进程──► 定时/延迟/异步（本轮空壳）
#
# 本地构建（需要 JDK 21+）
#   cd backend && mvn -q -DskipTests package
#
# 本地启动 API（脚手架可不连 PG/Redis）
#   java -jar meiyue-boot/target/meiyue-boot-0.1.0-SNAPSHOT.jar
#   curl http://localhost:8080/api/v1/ping
#   curl http://localhost:8080/actuator/health
#
# 对应规划文档
#   ecommerce-platform-plan.md §6.1 目录 / §5.4 技术栈 / 迭代 I1+
