# 重构报告：gulimail-ware（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-ware`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证锁库主链路完成 `application/domain/infrastructure` 分层后无功能退化。
- 验证控制器入口改造后，参数校验、无库存分支、成功分支响应语义保持一致。
- 为 `OrderLockStock` 核心规则建立可重复性能守护阈值。

## 2. 本轮验证范围

- `OrderLockStockApplicationService` 用例编排。
- `OrderLockStockDomainService` 命令归一化与规则校验。
- `WareSkuController#orderLockStock` 响应映射。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-ware` 模块测试总数 | 27 | 28 | 新增 1 条性能守护 |
| 锁库分层专项测试数 | 5 | 6 | 新增 1 条领域性能回归 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `OrderLockStockDomainServicePerformanceTest` 10k 次归一+校验耗时 | 通过 | <= 1500ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-ware test`） | 15.968s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-ware "-Dtest=OrderLockStockApplicationServiceTest,OrderLockStockDomainServiceTest,OrderLockStockDomainServicePerformanceTest,WareSkuControllerTest,GulimailWareApplicationTests" test`：通过（17/17）。
- `mvn -pl gulimail-ware test`：通过（28/28）。
- 结论：`gulimail-ware` 按清单 1~5 步可判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `OrderLockStockDomainServicePerformanceTest`，恢复功能回归模式（不影响业务逻辑）。
- 回滚点 2：若生产环境锁库链路抖动，可临时将 `OrderLockStockApplicationService` 回退为直连 `OrderLockStockPort`（保持控制器与响应不变）。
- 回滚点 3：若联调阶段出现参数映射问题，可临时回退 `WareSkuController#orderLockStock` 到 service 直连模式，保留分层代码继续灰度。
