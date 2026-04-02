# 重构报告：gulimail-order（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-order`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证订单提交流程分层改造后，应用编排与领域规则拆分不引入性能回退。
- 验证新增 `api/application/domain/infrastructure` 代码路径在回归测试下保持稳定。
- 给出可执行的回滚点，确保出现异常时可快速恢复。

## 2. 本轮验证范围

- `SubmitOrderApplicationService` 用例编排路径。
- `OrderSubmitDomainService` 规则路径（命令归一化、结果解析）。
- `OrderApiController` 与 `OrderWebController` 对提交流程的接入路径。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-order` 模块测试总数 | 31 | 34 | 新增 3 条回归守护 |
| 提交订单领域测试数 | 3 | 4 | 新增 1 条性能守护 |
| 提交订单应用编排测试数 | 0 | 2 | 新增编排层回归覆盖 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `OrderSubmitDomainServicePerformanceTest` 10k 次归一+解析耗时 | 通过 | <= 1500ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-order test`） | 22.108s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-order "-Dtest=OrderSubmitDomainServiceTest,OrderSubmitDomainServicePerformanceTest,SubmitOrderApplicationServiceTest,OrderApiControllerTest,OrderWebControllerTest,GulimailOrderApplicationTests" test`：通过（17/17）。
- `mvn -pl gulimail-order test`：通过（34/34）。
- 结论：`gulimail-order` 按清单 1~5 步可判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `OrderSubmitDomainServicePerformanceTest`，恢复功能回归模式（不影响业务逻辑）。
- 回滚点 2：若线上观察到提交链路抖动，先将 `SubmitOrderApplicationService` 回退为直连 `OrderSubmitPort`（保留兼容接口不变）。
- 回滚点 3：若 API 接口联调出现异常，可临时回退到 `OrderWebController` 入口路径并保留现有 application/domain 代码供后续灰度。
