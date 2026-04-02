# 重构报告：gulimail-third-party（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-third-party`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证短信主链路完成分层改造后，控制层与业务编排边界清晰且语义兼容。
- 验证验证码校验规则在高频场景下无明显性能退化。
- 输出可执行回滚点，确保异常时可快速恢复。

## 2. 本轮验证范围

- `SmsApplicationService` 发送与校验用例编排路径。
- `SmsDomainService` 手机号与验证码规则校验路径。
- `SmsController` 两个入口（`/sms/sendcode`、`/sms/checkcode`）响应映射路径。

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-third-party` 模块测试总数 | 15 | 22 | 新增 7 条回归守护 |
| 短信应用层/领域层测试数 | 0 | 6 | 新增分层覆盖 |
| 短信性能守护测试数 | 0 | 1 | 新增性能门槛断言 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `SmsDomainServicePerformanceTest` 10k 次校验耗时 | 通过 | <= 1200ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-third-party test`） | 27.296s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-third-party test`：通过（22/22）。
- 关键新增测试：
  - `SmsApplicationServiceTest`
  - `SmsDomainServiceTest`
  - `SmsDomainServicePerformanceTest`
- 结论：`gulimail-third-party` 可按清单 1~5 步判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `SmsDomainServicePerformanceTest`，保留功能分层改造。
- 回滚点 2：若应用层链路异常，可将 `SmsController` 临时回退为直连 `AliyunSmsService`。
- 回滚点 3：若端口适配层出现兼容问题，可回退 `SmsPortAdapter` 并保留 application/domain 层供后续灰度恢复。
