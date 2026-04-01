# gulimail 技术债矩阵与风险登记（基线）

本文档用于评审“问题分级与风险登记”在当前仓库下的基线结论：SonarQube 基线（待具备前置条件后落数）、核心链路热点定位清单（可用于 Arthas/日志 tracing）、以及面向重构阶段拆分的技术债矩阵（P0/P1/P2）。

## 1. SonarQube 基线（当前状态：未落数）

### 1.1 未落数原因（基线）

- 本机未检测到 SonarQube 服务（默认 `127.0.0.1:9000` 不可达），仓库也未包含既有 Sonar 配置（如 `sonar-project.properties` 或 Maven sonar 插件参数）。

### 1.2 建议的落数方式（用于阶段 0/CI）

- 方式 A（推荐）：SonarQube Server + Maven 扫描（根聚合）
  - 扫描范围：根聚合 reactor（12 个 gulimail 服务 + gulimail-common + renren-fast；`renren-generator` 独立扫描）。
  - 输出物：按 Blocker/Critical/Major/Minor 汇总代码坏味道、重复率、安全漏洞；并固化 Quality Gate。
- 方式 B：先以 SonarLint 规则集做预筛，阶段 0 统一门禁后再落 SonarQube。

### 1.3 基线报告模板（待填充）

- 总体：Bugs / Vulnerabilities / Code Smells / Duplications / Security Hotspots
- 分模块 Top10：`gulimail-order`、`gulimail-ware`、`gulimail-product` 优先
- 阻塞项（Blocker）清单：每项给出规则、位置、修复建议与风险说明

## 2. 核心业务链路热点“落点清单”（用于 Arthas/日志 tracing）

说明：当前代码层面，“下单/库存扣减”链路完整；“优惠券核销/积分变更”在订单主链路中未接入（仅字段预留/示例接口），因此 tracing 需先确认业务范围，避免把缺口误判为性能问题。

### 2.1 下单链路（订单服务）

- 入口（页面链路）
  - 订单确认：OrderWebController.confirmPage → OrderService.confirmOrder：[OrderWebController.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/web/OrderWebController.java#L34-L47)
  - 提交订单：OrderWebController.submitOrder → OrderService.submitOrder：[OrderWebController.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/web/OrderWebController.java#L74-L97)
- 热点方法（建议优先 trace/profile）
  - OrderServiceImpl.confirmOrder（并发聚合 + Redis token）：[OrderServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/service/impl/OrderServiceImpl.java#L86-L125)
  - OrderServiceImpl.submitOrder（Lua 防重 + DB 落库 + Feign 锁库存 + MQ 延迟）：[OrderServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/service/impl/OrderServiceImpl.java#L242-L337)

### 2.2 库存扣减链路（仓储服务）

- 锁库存入口：WareSkuController.orderLockStock → WareSkuService.orderLockStock：[WareSkuController.java](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/controller/WareSkuController.java#L92-L104)
- 热点方法
  - WareSkuServiceImpl.orderLockStock（多仓循环 + 多次 SQL + 写工作单）：[WareSkuServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/service/impl/WareSkuServiceImpl.java#L107-L146)
  - WareSkuServiceImpl.unlockStock / orderDeductStock：[WareSkuServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/service/impl/WareSkuServiceImpl.java#L148-L226)
- MQ 消费点
  - 支付成功扣减：StockDeductListener → WareSkuService.orderDeductStock：[StockDeductListener.java](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/listener/StockDeductListener.java#L15-L39)
  - 订单关闭解锁：StockReleaseListener → WareSkuService.unlockStock：[StockReleaseListener.java](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/listener/StockReleaseListener.java#L14-L33)

### 2.3 优惠券核销 / 积分变更（链路缺口说明）

- 订单侧算价目前未接入优惠券/积分（明确写为“先简化”）：[OrderServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/service/impl/OrderServiceImpl.java#L200-L212)
- coupon/member 存在相关 CRUD/Feign/示例接口，但未形成“下单时校验/锁定/支付后核销/回滚”的闭环。

## 3. 风险登记（重构阻塞项）

| 风险 | 影响 | 严重度 | 触发证据（基线） | 缓解策略 |
| --- | --- | --- | --- | --- |
| 第三方模块测试/启动依赖缺失配置占位符 | CI 无法全绿；本地启动失败 | 高 | `spring.cloud.alicloud.access-key` 无法解析（见质量基线报告） | 阶段 1 配置治理：拆分必需/可选配置，测试使用 stub 或 profile；敏感项走密钥治理 |
| 秒杀模块 Sentinel 依赖/适配缺失 | 上下文无法启动；重构会被阻塞 | 高 | 缺失 `BlockExceptionHandler.class`（见质量基线报告） | 阶段 1 统一依赖版本与 starter；补齐 Boot3 适配依赖 |
| 搜索模块测试依赖 ES 运行态索引 | CI 不稳定；无法度量覆盖率 | 高 | 缺少索引 `bank` 导致测试失败 | 阶段 1 引入 Testcontainers/测试数据初始化；或重写为可控的集成测试 |
| 单元测试对外部基础设施强耦合（RabbitMQ/Redis/ES） | 不可重复、不可移植 | 高 | order/ware 测试连接外部 RabbitMQ；seckill 配置外部 Redis | 阶段 1 引入 Testcontainers 基类与默认 profile，禁止测试直连“固定 IP” |
| Nacos 中存在敏感配置明文 | 安全合规风险 | 高 | Nacos 配置中存在第三方密钥类字段（不在仓库落出） | 阶段 1 引入密钥治理与脱敏审计；制定轮换与回滚流程 |
| 业务链路范围与现有代码不一致 | 评审目标偏差 | 中 | 优惠券核销/积分变更未接入订单主链路 | 阶段 0 明确需求范围：是“补齐业务能力”还是“仅重构现有能力” |

## 4. 技术债矩阵（业务优先级 × 重构成本）

口径说明：
- 业务优先级：对“下单/库存”主链路的影响优先；其次为网关安全与可观测性。
- 重构成本：综合改动面、回归风险、依赖外部系统程度。

| 技术债项 | 业务优先级 | 重构成本 | 分级 | 说明 |
| --- | --- | --- | --- | --- |
| 配置加载策略不一致（bootstrap 与 application 混用） | 高 | 中 | P0 | 直接影响启动一致性与灰度/回滚能力 |
| 测试不可移植（外部 RabbitMQ/ES/Redis 强依赖） | 高 | 中 | P0 | 直接影响覆盖率与 CI 质量门禁落地 |
| 秒杀模块 Sentinel 依赖缺失/不兼容 | 高 | 低~中 | P0 | 当前已阻塞测试；需先修复依赖与适配 |
| 敏感配置治理缺失 | 高 | 中 | P0 | 安全风险；需与 CI/审计联动 |
| 订单侧未接入优惠券/积分闭环（需求缺口） | 中 | 高 | P1 | 若纳入本次目标，需要补齐领域模型与事件驱动闭环 |
| Mapper XML 中手写 SQL 集中 | 中 | 中 | P1 | 与“禁止手写 SQL”目标冲突；需评估复杂查询替代策略 |
| common 模块作为“万能依赖”风险（未来易形成环/耦合） | 中 | 中 | P1 | 当前是星型依赖；需在分层后拆分为 API/Infra/SharedKernel |
| renren-fast 与 gulimail 共仓库但坐标体系不同 | 低 | 中 | P2 | 影响构建/门禁范围；可在阶段 0 决定是否拆仓/隔离 CI |

