# 重构报告：gulimail-search（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-search`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证搜索主链路在当前重构版本下的性能稳定性，避免出现查询延迟明显回退。
- 验证本轮新增测试不会破坏既有测试基线（controller/config/application 测试）。
- 形成可执行的风险回滚点，支持出现性能抖动时快速止血。

## 2. 本轮变更

- 在 `GulimailSearchApplicationTests` 新增 `searchLatencyRegression` 集成性能回归测试：
  - 自动准备 `bank_perf` 索引样本数据（120 条）。
  - 连续执行 20 轮检索请求，跳过前 2 次预热，统计 p50/p95。
  - 断言 p95 不超过 3000ms，作为上线前性能回归门槛。
- 新增搜索主链路分层：
  - `SearchApplicationService` + `SearchDomainService`。
  - `SearchQueryPort/ProductUpPort` 与对应 `Adapter`。
  - `SearchController`、`ElasticSaveController` 改为调用应用层。
- 新增分层回归与性能守护测试：
  - `SearchApplicationServiceTest`、`SearchDomainServiceTest`、`SearchDomainServicePerformanceTest`。

## 3. 对比数据

### 3.1 代码与测试规模对比

| 指标 | 基线（变更前） | 当前（变更后） | 结论 |
| --- | ---: | ---: | --- |
| `GulimailSearchApplicationTests` 用例数 | 3 | 4 | 新增 1 条性能回归用例 |
| 模块总测试数（`mvn -pl gulimail-search test`） | 11 | 19 | 新增 8 条回归守护 |

### 3.2 性能指标对比（同机同环境）

| 指标 | 采样 1 | 采样 2 | 结论 |
| --- | ---: | ---: | --- |
| p50 查询延迟 | 8ms | 3ms | 稳定在毫秒级 |
| p95 查询延迟 | 10ms | 14ms | 明显低于 3000ms 门槛 |
| 样本数（预热后） | 18 | 18 | 采样口径一致 |

## 4. 回归验证结果

- `mvn -pl gulimail-search -Dtest=GulimailSearchApplicationTests test`：通过。
- `mvn -pl gulimail-search test`：通过（19/19）。
- 结论：本轮符合“性能与回归验证”交付标准，`gulimail-search` 第 5 步可判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `searchLatencyRegression` 新增用例，恢复到功能回归模式（不影响业务代码）。
- 回滚点 2：若线上 ES 压力导致 p95 波动，可临时提高该测试阈值并保留采样日志，再做容量评估后恢复阈值。
- 回滚点 3：若索引写入导致 CI 时长波动，可将样本规模从 120 降至 80，保持统计口径不变（预热 2 次 + 采样 18 次）。
