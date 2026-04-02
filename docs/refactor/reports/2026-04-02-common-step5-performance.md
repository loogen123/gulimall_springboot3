# 重构报告：gulimail-common（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-common`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证公共模块自动装配出口补齐后，不影响现有业务模块启动语义。
- 验证新增缓存旁路性能守护在高频调用场景下满足门槛。
- 验证 common 既有安全/工具测试在本轮变更后全部回归通过。

## 2. 本轮验证范围

- `CommonMybatisAutoConfiguration` 自动装配能力（分页拦截器、元数据处理器）。
- `AutoConfiguration.imports` 导出项变更后的兼容性。
- `CacheUtils` 的 10k 次缓存旁路调用性能守护。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-common` 模块测试总数 | 24 | 27 | 新增 3 条回归守护 |
| 自动装配测试数 | 0 | 2 | 补齐自动装配出口验证 |
| 性能守护测试数 | 0 | 1 | 新增 CacheUtils 性能门槛 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `CacheUtilsPerformanceTest` 10k 次缓存旁路耗时 | 通过 | <= 2000ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-common test`） | 6.137s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-common test`：通过（27/27）。
- 关键新增测试：
  - `CommonMybatisAutoConfigurationTest`
  - `CacheUtilsPerformanceTest`
- 结论：`gulimail-common` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `CacheUtilsPerformanceTest`，保留功能改造。
- 回滚点 2：若自动装配出现兼容问题，可临时从 `AutoConfiguration.imports` 移除 `CommonMybatisAutoConfiguration`。
- 回滚点 3：若分页拦截器冲突，可保留 `MetaObjectHandler` 自动装配并暂时回退分页拦截器自动注入。
