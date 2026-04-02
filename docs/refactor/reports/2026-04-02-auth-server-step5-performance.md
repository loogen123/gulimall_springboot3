# 重构报告：gulimail-auth-server（第5步 性能与回归验证）

- 日期：2026-04-02
- 模块：`gulimail-auth-server`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证认证核心链路完成 `application/domain/infrastructure` 分层后行为不回退。
- 验证登录、注册、短信发送、GitHub OAuth 回调链路重构后协议与会话语义保持一致。
- 建立领域服务性能守护阈值，降低后续规则扩展带来的性能回退风险。

## 2. 本轮验证范围

- `LoginApplicationService`
- `GithubOAuthApplicationService`
- `AuthDomainService`
- `LoginController`、`OAuth2Controller`

## 3. 对比数据

### 3.1 测试规模对比

| 指标 | 基线（本轮前） | 当前（本轮后） | 结论 |
| --- | ---: | ---: | --- |
| `gulimail-auth-server` 模块测试总数 | 15 | 22 | 新增 7 条回归守护 |
| 认证应用/领域专项测试数 | 0 | 7 | 补齐应用编排、领域规则与性能守护 |

### 3.2 性能指标（同机同环境）

| 指标 | 采样结果 | 门槛 | 结论 |
| --- | ---: | ---: | --- |
| `AuthDomainServicePerformanceTest` 10k 次归一化+token解析耗时 | 通过 | <= 1500ms | 满足门槛 |
| 模块全量测试耗时（`mvn -pl gulimail-auth-server test`） | 16.381s | 无硬门槛 | 回归稳定 |

## 4. 回归验证结果

- `mvn -pl gulimail-auth-server test`：通过（22/22）。
- 结论：`gulimail-auth-server` 按清单 1~5 步可判定完成。

## 5. 风险回滚点

- 回滚点 1：仅回滚 `AuthDomainServicePerformanceTest`，恢复功能回归模式（不影响业务逻辑）。
- 回滚点 2：若 OAuth 链路联调异常，可临时将 `OAuth2Controller` 回退为直连流程实现，保留应用层灰度。
- 回滚点 3：若登录/注册响应差异导致前端回归问题，可临时让 `LoginController` 直连 Feign，保持新增分层代码并行。
