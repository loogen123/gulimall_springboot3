# 重构报告：gulimail-member（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-member`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证会员积分内部接口链路完成分层后，行为语义保持一致。
- 验证地址列表按当前登录用户过滤逻辑在服务层真实生效。
- 验证领域规则在高频调用场景下满足性能门槛。

## 2. 本轮验证范围

- `MemberIntegrationApplicationService` 积分试算/扣减/回滚用例编排。
- `MemberIntegrationDomainService` 参数校验与积分金额试算规则。
- `MemberController` 内部积分接口入口。
- `MemberReceiveAddressServiceImpl#queryPage` 用户地址过滤条件。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-member` 模块测试总数 | 22 | 28 | 新增 6 条回归守护 |
| 积分链路应用层测试数 | 0 | 4 | 新增应用层编排覆盖 |
| 积分链路领域层测试数 | 0 | 3 | 新增规则与性能守护 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `MemberIntegrationDomainServicePerformanceTest` 10k 次规则耗时 | 通过 | <= 1200ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-member test`） | 8.433s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-member test`：通过（28/28）。
- 关键新增测试：
  - `MemberIntegrationApplicationServiceTest`
  - `MemberIntegrationDomainServiceTest`
  - `MemberIntegrationDomainServicePerformanceTest`
  - `MemberReceiveAddressControllerSecurityTest` 新增列表过滤断言
- 结论：`gulimail-member` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `MemberIntegrationDomainServicePerformanceTest`，保留功能链路改造。
- 回滚点 2：若积分内部接口出现兼容问题，可将 `MemberController` 的 `/internal/integration/*` 临时回退为旧控制器内联逻辑。
- 回滚点 3：若端口适配层出现问题，可回退 `MemberIntegrationPortAdapter`，保留 application/domain 层供灰度恢复。
