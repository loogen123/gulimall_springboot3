# 《gulimail 重构需求文档》（评审稿）

## 0. 文档信息

- 项目：gulimail
- 目标：系统性重构（架构分层、质量门禁、配置治理、可观测性）
- 适用范围：根聚合 Maven reactor 下的 gulimail 系列模块；renren 系列是否纳入需在阶段 0 决策

## 1. 背景与现状

### 1.1 仓库结构基线

- 根聚合：Maven 多模块（`packaging=pom`），Java 17，Spring Boot 3.2.0，Spring Cloud 2023.0.0，Spring Cloud Alibaba 2023.0.1.0：[pom.xml](file:///d:/GitProgram/gulimail/pom.xml#L12-L35)
- 模块依赖呈“星型结构”：业务模块 → `gulimail-common`，当前无模块级循环依赖（详见依赖扫描基线结论）。

### 1.2 代码资产基线（Spring/Mapper/Feign/MQ/定时）

- 代码资产清单（已落地）：[code-asset-inventory.md](file:///d:/GitProgram/gulimail/docs/refactor/code-asset-inventory.md)
- 关键结论：
  - 启动类：gulimail 系列 12 个 + renren-fast + renren-generator（独立系统）
  - MQ：order/ware 存在 RabbitMQ 监听器，具备订单延迟关单与库存扣减链路
  - Feign：跨服务调用关系清晰，可用于反推核心链路边界

### 1.3 配置治理基线（本地 ⇄ Nacos）

- 配置映射表（已落地）：[config-mapping.md](file:///d:/GitProgram/gulimail/docs/refactor/config-mapping.md)
- 关键结论：
  - 配置加载策略不一致（bootstrap 与 application 混用）
  - Nacos 部分 namespace 配置缺失（gateway/coupon）
  - 存在敏感配置明文风险（需密钥治理与审计）

### 1.4 质量基线（测试）

- 测试基线报告（已落地）：[quality-baseline.md](file:///d:/GitProgram/gulimail/docs/refactor/quality-baseline.md)
- 关键结论：
  - 当前 `mvn test` 在根聚合维度失败（third-party 缺失配置；search/seckill 测试不稳定/不可运行）
  - 测试存在外部依赖强耦合（RabbitMQ/ES/Redis），不可作为 CI 质量门禁依据

### 1.5 核心业务链路现状差异

- 下单/库存扣减链路：代码层面闭环较完整（订单提交 → 锁库存 → 支付后扣减/关闭解锁）
- 优惠券核销/积分变更：当前未接入订单主链路（更多为字段预留/示例接口），需在阶段 0 明确“是否补齐业务能力”还是“仅重构既有能力”
- 热点落点清单（用于 Arthas/日志 tracing）：[tech-debt-and-risks.md](file:///d:/GitProgram/gulimail/docs/refactor/tech-debt-and-risks.md)

## 2. 重构目标（To-Be）

### 2.1 架构目标

- 领域驱动分层：API → Application → Domain → Infrastructure
  - 聚合根之间禁止跨层调用与“越层依赖”（通过架构规则/静态检查约束）
  - 领域层不依赖 Spring、MyBatis、Feign 等基础设施实现（通过接口反转）
- 兼容性目标：
  - Spring Cloud 2023.x & Spring Boot 3.2 兼容（仓库已在版本维度对齐，但存在适配缺口需修复）
- 质量与性能目标：
  - 单元测试覆盖率 ≥ 80%（以稳定可重复为前提）
  - 核心接口性能提升 ≥ 30%（以基线压测报告为对照）

### 2.2 代码规范目标

- 规约：遵循 java-dev 规约（命名、日志、异常、并发、幂等、事务、安全）
- 门禁：checkstyle 零警告；PMD/SpotBugs 零高严重；SonarQube 质量门禁落地（阻塞/严重问题清零）

### 2.3 配置治理目标

- 所有环境差异项收归 Nacos；本地仅保留最小启动配置（应用名、profile、Nacos 连接信息）
- 关键配置变更支持灰度发布与版本回滚（以 namespace/group/dataId 版本化与发布流程约束实现）
- 敏感配置脱敏与审计（配置权限隔离、变更审计、轮换策略）

## 3. 范围与边界

### 3.1 重构范围（默认纳入）

- gulimail 业务服务：member/product/order/ware/coupon/search/cart/auth-server/seckill/third-party/gateway/ai
- 公共模块：gulimail-common

### 3.2 待决策范围（阶段 0 输出结论）

- renren-fast、renren-generator：
  - 是否作为“独立产品线”继续共仓库/共门禁
  - 是否拆分 CI、质量门禁与发布流水线

### 3.3 非目标（本轮不做或仅做最小化调整）

- 不做大规模 API 契约变更（除非为修复 P0 缺陷或安全问题）
- 不做一次性全量数据迁移（需要分阶段、可回滚的 Flyway 迁移策略）

## 4. 验收标准（阶段门禁）

### 4.1 编译/启动验收

- 每个服务：可编译、可启动、能注册到注册中心、能从配置中心读取配置
- CI：主分支/重构分支均满足“构建全绿 + 质量门禁达标”

### 4.2 测试验收

- 单元测试：覆盖率 ≥ 80%，且不依赖固定 IP 的外部基础设施（Testcontainers 或可控 stub）
- 集成测试：覆盖“下单、库存锁定/解锁/扣减”等主流程
- 接口回归：提供可执行的接口测试套件（与环境解耦、可在 CI 执行）

### 4.3 性能验收

- 建立性能基线：固定场景、固定数据集、固定依赖版本
- 核心接口：基线对比提升 ≥ 30%（以 P95/P99、吞吐与错误率为主指标）

## 5. 交付物清单（评审件）

已落地/可直接评审：
- 代码资产清单：[code-asset-inventory.md](file:///d:/GitProgram/gulimail/docs/refactor/code-asset-inventory.md)
- 配置映射表：[config-mapping.md](file:///d:/GitProgram/gulimail/docs/refactor/config-mapping.md)
- 质量基线报告：[quality-baseline.md](file:///d:/GitProgram/gulimail/docs/refactor/quality-baseline.md)
- 技术债矩阵与风险登记：[tech-debt-and-risks.md](file:///d:/GitProgram/gulimail/docs/refactor/tech-debt-and-risks.md)

待在阶段 0/CI 完善后补齐（有模板、有落数路径）：
- Sonar 基线报告（含阻塞/严重/主要/次要分级、重复率、安全漏洞）
- 链路热点耗时图（基于运行态日志/Arthas trace 的输出物）

