# 重构报告：gulimail-ai（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-ai`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证 AI 会话主链路完成分层后，功能回归稳定，不引入行为回退。
- 验证新增 `application/domain/infrastructure` 边界下，主链路编排与领域校验性能满足门槛。
- 输出可执行回滚点，保障异常情况下快速恢复。

## 2. 本轮验证范围

- `AiChatApplicationService` 会话创建/复用、消息写入与查询编排路径。
- `AiChatDomainService` 参数合法性、会话归属校验路径。
- `AiChatController` 会话列表、会话消息、流式聊天入口回归路径。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-ai` 模块测试总数 | 16 | 19 | 新增 3 条回归守护 |
| 应用层测试数 | 3 | 5 | 新增 2 条主链路集成测试 |
| 领域层测试数 | 5 | 6 | 新增 1 条性能守护 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `AiChatDomainServicePerformanceTest` 10k 次校验耗时 | 通过 | <= 1200ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-ai test`） | 5.926s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-ai test`：通过（19/19）。
- 关键新增测试：
  - `AiChatApplicationIntegrationTest`（会话流转与越权拒绝主链路）
  - `AiChatDomainServicePerformanceTest`（10k 次领域校验性能守护）
- 结论：`gulimail-ai` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `AiChatDomainServicePerformanceTest`，保留功能改造并恢复轻量功能回归。
- 回滚点 2：若会话编排链路异常，可将 `AiChatController` 的会话读写临时回退为直接调用原有持久化实现。
- 回滚点 3：若适配层出现兼容问题，可先回退 `AiChatPersistencePortAdapter`，保留应用层和领域层代码用于后续灰度恢复。
