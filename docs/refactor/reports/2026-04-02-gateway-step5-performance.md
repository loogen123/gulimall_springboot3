# 重构报告：gulimail-gateway（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-gateway`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证网关限流语义收口后，HTTP 状态码与业务码语义一致且稳定。
- 验证新增 TraceId 透传链路可用，不影响原有认证与防重放路径。
- 验证 TraceId 注入规则在高频调用下满足性能门槛。

## 2. 本轮验证范围

- `SentinelGatewayConfig` 的限流状态码回退与响应体编码逻辑。
- `TraceIdFilter` 的请求透传与响应回写链路。
- 网关过滤器回归（Auth、Replay、Trace）和全模块测试稳定性。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-gateway` 模块测试总数 | 15 | 19 | 新增 4 条回归守护 |
| Trace 链路测试数 | 0 | 4 | 覆盖功能与性能 |
| Sentinel 限流语义测试数 | 3 | 3 | 保持覆盖并修正断言口径 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `TraceIdFilterPerformanceTest` 10k 次注入耗时 | 通过 | <= 3000ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-gateway test`） | 10.122s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-gateway test`：通过（19/19）。
- 关键新增测试：
  - `TraceIdFilterTest`
  - `TraceIdFilterPerformanceTest`
- 结论：`gulimail-gateway` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `TraceIdFilterPerformanceTest`，保留功能改造。
- 回滚点 2：若 TraceId 透传链路异常，可临时下线 `TraceIdFilter`，保留 Auth/Replay 路径。
- 回滚点 3：若限流语义出现兼容问题，可回退 `SentinelGatewayConfig` 新逻辑并恢复旧回退策略。
