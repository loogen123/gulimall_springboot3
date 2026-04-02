# 重构报告：gulimail-seckill（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-seckill`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证秒杀查询主链路完成分层改造后，接口语义保持兼容并收敛空返回语义。
- 验证领域规则在高频调用下满足性能门槛。
- 输出可执行回滚点，保障异常情况下快速恢复。

## 2. 本轮验证范围

- `SeckillSkuApplicationService` 查询当前秒杀商品与 SKU 秒杀信息编排路径。
- `SeckillSkuDomainService` 参数校验与空集合归一化规则。
- `SeckillController` 的 `/currentSeckillSkus` 与 `/sku/seckill/{skuId}` 入口。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-seckill` 模块测试总数 | 11 | 19 | 新增 8 条回归守护 |
| 查询链路应用层/领域层测试数 | 0 | 7 | 新增分层覆盖 |
| 查询链路性能守护测试数 | 0 | 1 | 新增性能门槛断言 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `SeckillSkuDomainServicePerformanceTest` 10k 次规则耗时 | 通过 | <= 1200ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-seckill test`） | 4.474s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-seckill test`：通过（19/19）。
- 关键新增测试：
  - `SeckillSkuApplicationServiceTest`
  - `SeckillSkuDomainServiceTest`
  - `SeckillSkuDomainServicePerformanceTest`
- 结论：`gulimail-seckill` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `SeckillSkuDomainServicePerformanceTest`，保留功能分层改造。
- 回滚点 2：若查询应用层链路异常，可将 `SeckillController` 的查询接口临时回退为直连 `SeckillService`。
- 回滚点 3：若端口适配层出现兼容问题，可回退 `SeckillSkuQueryPortAdapter` 并保留 application/domain 层供后续灰度恢复。
