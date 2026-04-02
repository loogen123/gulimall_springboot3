# 重构报告：gulimail-product（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-product`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证 SKU 详情主链路分层改造后，控制层与业务编排边界清晰且行为兼容。
- 验证新增 `application/domain/infrastructure` 代码路径在回归测试中稳定。
- 验证领域层规则在 10k 次调用下满足性能门槛，确保无明显性能退化。

## 2. 本轮验证范围

- `SkuItemApplicationService` 用例编排路径（参数归一化 -> 规则校验 -> 端口查询）。
- `SkuItemDomainService` 规则路径（skuId 合法性判定）。
- `SkuInfoController` 与 `ItemController` 的 SKU 详情入口路径。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-product` 模块测试总数 | 19 | 24 | 新增 5 条回归守护 |
| SKU详情分层测试数 | 0 | 4 | 新增应用层/领域层覆盖 |
| SKU详情性能守护测试数 | 0 | 1 | 新增性能门槛断言 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `SkuItemDomainServicePerformanceTest` 10k 次校验耗时 | 通过 | <= 1200ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-product test`） | 22.156s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-product test`：通过（24/24）。
- 关键新增测试：
  - `SkuItemApplicationServiceTest`
  - `SkuItemDomainServiceTest`
  - `SkuItemDomainServicePerformanceTest`
- 结论：`gulimail-product` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `SkuItemDomainServicePerformanceTest`，保留功能链路改造。
- 回滚点 2：若新应用层链路出现问题，可将 `SkuInfoController#getSkuItem` 临时回退为直连 `SkuInfoService#item`。
- 回滚点 3：若端口适配层兼容异常，可回退 `SkuItemQueryPortAdapter` 并保留 application/domain 层供后续灰度恢复。
