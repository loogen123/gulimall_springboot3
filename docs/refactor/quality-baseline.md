# gulimail 质量基线报告（测试）

本报告记录当前仓库在“重构前”阶段的可验证质量基线：单元测试可运行性、通过率、耗时、主要异常堆栈摘要，以及对 CI/门禁引入的直接影响点。

## 1. 总览结论

- 根聚合执行 `mvn test`：失败（在 `gulimail-third-party` 模块中断）。
- 已发现“测试对外部环境强耦合”的情况（RabbitMQ/Redis/Elasticsearch 等），导致测试不可重复、不可移植，无法作为 CI 质量门禁的可靠依据。
- 当前基线下，若直接引入“CI 必须全绿”的质量门禁，将因以下原因阻塞：
  - 缺失必须配置项（第三方模块启动依赖的占位符无法解析）。
  - 测试依赖外部运行态（ES 索引存在性、RabbitMQ 可达性等）。
  - 依赖/适配器缺失导致应用上下文无法加载（秒杀模块与 Sentinel 相关）。

## 2. 单元测试执行结果（按模块）

说明：耗时取自 Maven Surefire/日志输出的 `Time elapsed`，仅用于“基线对比”，不作为性能指标。

| 模块 | 结果 | 测试统计 | 主要问题/备注 |
| --- | --- | --- | --- |
| gulimail-third-party | 失败 | `Tests run: 1, Errors: 1`（≈ 2.019s） | `Could not resolve placeholder 'spring.cloud.alicloud.access-key'` 导致 ApplicationContext 加载失败 |
| gulimail-search | 失败 | `Tests run: 3, Errors: 1`（≈ 5.855s） | `index_not_found_exception`：缺少 Elasticsearch 索引 `bank` |
| gulimail-seckill | 失败 | `Tests run: 1, Errors: 1`（≈ 1.493s） | 缺少 Sentinel WebMVC 适配类 `BlockExceptionHandler`，导致配置类解析失败、上下文启动失败 |
| gulimail-ai | 通过 | `Tests run: 1, Errors: 0`（≈ 0.793s） | - |
| gulimail-auth-server | 通过 | `Tests run: 1, Errors: 0`（≈ 5.113s） | - |
| gulimail-cart | 通过 | `Tests run: 1, Errors: 0`（≈ 4.717s） | - |
| gulimail-coupon | 通过 | `Tests run: 1, Errors: 0`（≈ 7.298s） | - |
| gulimail-gateway | 通过 | `Tests run: 1, Errors: 0`（≈ 9.285s） | - |
| gulimail-member | 通过 | `Tests run: 1, Errors: 0`（≈ 0.271s） | - |
| gulimail-order | 通过 | `Tests run: 2, Errors: 0`（≈ 8.407s + 3.118s） | 测试过程连接 RabbitMQ（外部依赖地址出现在日志中） |
| gulimail-product | 通过 | `Tests run: 2, Errors: 0`（≈ 14.59s） | - |
| gulimail-ware | 通过 | `Tests run: 1, Errors: 0`（≈ 8.609s） | 测试过程连接 RabbitMQ（外部依赖地址出现在日志中） |
| renren-fast | 通过 | 未观测到 Surefire 测试统计输出 | 需要进一步确认是否存在“未被 Surefire 识别/被跳过”的测试 |
| renren-generator（独立构建） | 通过 | `Tests run: 1, Errors: 0`（≈ 1.592s） | 该模块未纳入根聚合 reactor（需单独在目录内执行） |

## 3. 关键异常堆栈摘要

### 3.1 gulimail-third-party：缺失必需配置占位符

- 现象：`Failed to load ApplicationContext`
- 根因：占位符无法解析（`spring.cloud.alicloud.access-key`）
- 影响：任何依赖该配置的 bean 初始化均会失败，导致测试/启动不可用

### 3.2 gulimail-search：依赖 ES 运行态数据

- 现象：`Elasticsearch exception [type=index_not_found_exception, reason=no such index [bank]]`
- 根因：测试直接查询 ES 索引 `bank`，未做测试前置数据/索引准备
- 影响：在空环境、CI 环境必然失败；无法作为可重复的自动化测试

### 3.3 gulimail-seckill：Sentinel 适配依赖缺失

- 现象：`BeanDefinitionStoreException` / `FileNotFoundException`（类路径缺少 `BlockExceptionHandler.class`）
- 根因：配置类引用了 Sentinel WebMVC 适配器相关类，但依赖未引入或版本不匹配
- 影响：应用上下文无法启动，测试/运行均受阻

## 4. 接口测试基线（仓库内可见范围）

- 未在仓库内发现可直接执行的接口测试套件（如 Postman/Newman、RestAssured、Karate、JMeter 脚本等）。
- 建议在阶段 0/1 明确“接口测试基线”的承载方式：`Testcontainers + SpringBootTest`、或独立的 e2e 测试工程，并纳入 CI。

