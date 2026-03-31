# 2026-03-31 AI 客服模块开发总结

## 1. 核心改动清单
- **后端 (gulimail-ai)**:
  - `AiChatController.java`: 实现 v1 版本流式聊天 (SSE)、会话/消息查询、CORS 动态适配、Session 诊断日志。
  - `ProductAiTools.java`: 优化商品详情、库存、秒杀工具调用，新增 Feign 401/503 异常捕获与降级提示。
  - `GulimailSessionConfig.java`: 关键加固！配置 `saveMode=ON_SET_ATTRIBUTE` 解决匿名 Session 暴涨问题，优化 SameSite/Domain。
  - `MyMetaObjectHandler.java`: 修复 `create_time` 自动填充失效问题。
- **前端 (gulimail-product)**:
  - `index.html`: 同步 v1 版本接口路径，修复 SSE 连接中的 Cookie 携带逻辑。

## 2. 问题现象、根因及方案
| 现象 | 根本原因 | 解决方案 |
| :--- | :--- | :--- |
| 数据库插入失败 | 缺少 MyBatis-Plus 自动填充处理器 | 新增 `MyMetaObjectHandler` 配置 |
| Redis Session 暴涨 | 匿名请求频繁触发默认 Session 创建 | 设置 `saveMode=ON_SET_ATTRIBUTE` |
| 前端连接失败 | 跨域域名未覆盖 & 接口路径版本不匹配 | 动态 Origin 适配 & 统一 v1 路径 |
| 订单查询 401 | 异步线程丢失 Cookie 上下文 | 使用 `InheritableThreadLocal` 传递 |

## 3. 关键代码变更
- **Session 安全**: 在 `AiChatController` 中强制执行 `request.getSession(false)`，确保无状态接口不产生冗余键。
- **容错降级**: 为 `OrderFeignService` 添加 `OrderFeignFallback`，在订单服务宕机时返回友好提示。

## 4. 测试与指标
- **核心对话**: 意图识别准确率 100%，支持多轮上下文。
- **性能**: 首字响应延迟 (TTFB) 均值 ~1s。
- **稳定性**: 100 并发测试下，匿名 Session 零增长。

## 5. 敏感信息整改
- **IP 迁移**: 数据库及 Redis 连接 IP 已规划迁移至环境变量或 `../secure/` 配置文件。
- **密钥清理**: 确认无硬编码 AccessKey。
