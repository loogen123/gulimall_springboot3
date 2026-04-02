# 重构报告：gulimail-cart（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-cart`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证购物车“勾选态闭环”完成 `application/domain/infrastructure` 分层后，行为保持一致。
- 验证 `CartController` 改造后参数校验与响应码语义不回退。
- 建立领域规则性能守护阈值，避免后续规则扩展导致显著退化。

## 2. 本轮验证范围

- `CartSelectionApplicationService`（用例编排）
- `CartSelectionDomainService`（参数校验规则）
- `CartSelectionPortAdapter`（基础设施适配）
- `CartController#checkItem` 与 `/currentUserCartItems`（入口行为）

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-cart` 模块测试总数 | 17 | 25 | 新增 8 条回归守护 |
| 勾选态专项测试数 | 3 | 10 | 覆盖应用编排、领域规则、性能门槛 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `CartSelectionDomainServicePerformanceTest` 10k 次归一化+校验耗时 | 通过 | <= 1000ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-cart test`） | 13.246s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-cart "-Dtest=CartSelectionApplicationServiceTest,CartSelectionDomainServiceTest,CartSelectionDomainServicePerformanceTest,CartControllerTest,GulimailCartApplicationTests" test`：通过（10/10）。
- `mvn -pl gulimail-cart test`：通过（25/25）。
- 结论：`gulimail-cart` 按清单 1~5 步可判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `CartSelectionDomainServicePerformanceTest`，恢复功能回归模式（不影响业务逻辑）。
- 回滚点 2：若联调期间勾选态异常，可临时将 `CartSelectionApplicationService` 回退为直连 `CartSelectionPort`。
- 回滚点 3：若接口侧出现兼容问题，可临时将 `CartController` 回退为直接调用 `CartService`，保留分层代码继续灰度。
