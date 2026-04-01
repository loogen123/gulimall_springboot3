# gulimail 配置映射表（本地 ⇄ Nacos）

本文档用于评审“配置治理”前的基线现状：梳理当前各服务的本地配置文件位置、Nacos 配置中心的 dataId/group/namespace（tenant）以及两者的差异与风险点。

## 1. 结论摘要（基线）

- 当前各服务的 Nacos 配置使用方式不一致：部分使用 `bootstrap.*`，部分把 Nacos 地址写在 `application.properties`；与 Spring Boot 3.2 + Spring Cloud 2023 的推荐实践（`spring.config.import=nacos:` + 统一配置加载策略）不一致。
- Nacos 中已存在配置（按 namespace/tenant）仅覆盖：`gulimail-product`、`gulimail-ai`、`gulimail-order`、`gulimail-third-party`；`gulimail-gateway` 与 `gulimail-coupon` 的 namespace 目前为空（0 条配置），需要确认是否“namespace 写错/未导入/实际使用公共命名空间”。
- Nacos 配置内容中已出现敏感项（例如第三方 API Key 一类的明文配置）。该类配置必须纳入“密钥治理”（最少：脱敏、权限隔离、变更审计、轮换机制），并避免写入仓库/日志。

## 2. 本地配置文件基线（按模块）

### 2.1 含 bootstrap.* 的模块（显式 tenant / file-extension）

| 模块 | 本地文件 | server-addr | namespace(tenant) | file-extension | 期望 dataId | group |
| --- | --- | --- | --- | --- | --- | --- |
| gulimail-product | [bootstrap.yml](file:///d:/GitProgram/gulimail/gulimail-product/src/main/resources/bootstrap.yml) | 127.0.0.1:8848 | `3531d4a7-97cf-4b4e-bfc1-fe4eec5f2f4e` | yaml | `gulimail-product.yaml` | DEFAULT_GROUP |
| gulimail-ai | [bootstrap.properties](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/resources/bootstrap.properties) | 127.0.0.1:8848 | `60506194-2d6f-4685-82e7-a57b9bd9000c` | yaml | `gulimail-ai.yaml` | DEFAULT_GROUP |
| gulimail-order | [bootstrap.properties](file:///d:/GitProgram/gulimail/gulimail-order/src/main/resources/bootstrap.properties) | 127.0.0.1:8848 | `12ce9739-23b3-45cd-812f-5e20d1b452d0` | properties | `gulimail-order.properties` | DEFAULT_GROUP |
| gulimail-third-party | [bootstrap.properties](file:///d:/GitProgram/gulimail/gulimail-third-party/src/main/resources/bootstrap.properties) | 127.0.0.1:8848 | `130a8bdb-54e4-4fa8-8660-037f8f3c67d5` | yaml | `gulimail-third-party.yaml` | DEFAULT_GROUP |
| gulimail-gateway | [bootstrap.properties](file:///d:/GitProgram/gulimail/gulimail-gateway/src/main/resources/bootstrap.properties) | 127.0.0.1:8848 | `3ea21cb1-b5ff-46d8-a3e0-a9afd589c272` | yml | `gulimail-gateway.yml` | DEFAULT_GROUP |
| gulimail-coupon | [bootstrap.properties](file:///d:/GitProgram/gulimail/gulimail-coupon/src/main/resources/bootstrap.properties) | 127.0.0.1:8848 | `8f496a47-5138-4cbf-9a82-0a81a7c8be60` | (未显式配置) | 默认推断：`gulimail-coupon.properties` | DEFAULT_GROUP |

说明：
- `group` 未显式配置时，按 Nacos 默认使用 `DEFAULT_GROUP`。
- `dataId` 在未配置扩展配置（shared/extension）时，按“`spring.application.name` + `.` + file-extension”推断。

### 2.2 未使用 bootstrap.* 的模块（Nacos 地址散落在 application.properties）

| 模块 | 本地文件 | 发现的 Nacos 配置项 |
| --- | --- | --- |
| gulimail-seckill | [application.properties](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/resources/application.properties) | `spring.cloud.nacos.config.server-addr=127.0.0.1:8848`（未配置 namespace、file-extension） |
| gulimail-auth-server | [application.properties](file:///d:/GitProgram/gulimail/gulimail-auth-server/src/main/resources/application.properties) | `spring.cloud.nacos.config.server-addr=127.0.0.1:8848`（未配置 namespace、file-extension） |
| gulimail-search | [application.properties](file:///d:/GitProgram/gulimail/gulimail-search/src/main/resources/application.properties) | `spring.cloud.nacos.config.server-addr=127.0.0.1:8848`（未配置 namespace、file-extension） |

说明：
- 在 Spring Boot 3.2 + Spring Cloud 2023 的组合下，建议统一采用 `spring.config.import=nacos:` 的方式加载配置，避免“有的服务走 bootstrap、有的服务不走 bootstrap”导致的启动差异与不可预期问题。

## 3. Nacos 现网（本机 127.0.0.1:8848）配置清单（按 tenant）

本节为自动拉取的“元数据清单”（只列 dataId/group/tenant，不落出内容）。

| tenant(namespace) | dataId | group | 状态 |
| --- | --- | --- | --- |
| `3531d4a7-97cf-4b4e-bfc1-fe4eec5f2f4e` | `gulimail-product.yaml` | DEFAULT_GROUP | 存在 |
| `60506194-2d6f-4685-82e7-a57b9bd9000c` | `gulimail-ai.yaml` | DEFAULT_GROUP | 存在 |
| `12ce9739-23b3-45cd-812f-5e20d1b452d0` | `gulimail-order.properties` | DEFAULT_GROUP | 存在 |
| `130a8bdb-54e4-4fa8-8660-037f8f3c67d5` | `gulimail-third-party.yaml` | DEFAULT_GROUP | 存在 |
| `3ea21cb1-b5ff-46d8-a3e0-a9afd589c272` | （空） | - | 缺失 |
| `8f496a47-5138-4cbf-9a82-0a81a7c8be60` | （空） | - | 缺失 |

## 4. 差异比对（bootstrap 推断 ⇄ Nacos 实际）

| 模块 | 推断 dataId/group/tenant | Nacos 实际 | 差异结论 |
| --- | --- | --- | --- |
| gulimail-product | `gulimail-product.yaml` / DEFAULT_GROUP / `3531...f2f4e` | 存在 | 一致 |
| gulimail-ai | `gulimail-ai.yaml` / DEFAULT_GROUP / `6050...000c` | 存在 | 一致 |
| gulimail-order | `gulimail-order.properties` / DEFAULT_GROUP / `12ce...52d0` | 存在 | 一致 |
| gulimail-third-party | `gulimail-third-party.yaml` / DEFAULT_GROUP / `130a...67d5` | 存在 | 一致 |
| gulimail-gateway | `gulimail-gateway.yml` / DEFAULT_GROUP / `3ea2...c272` | 该 tenant 下 0 条 | 需要确认：namespace 是否正确、是否应使用公共命名空间、或配置尚未导入 |
| gulimail-coupon | 默认推断 `gulimail-coupon.properties` / DEFAULT_GROUP / `8f49...be60` | 该 tenant 下 0 条 | 需要确认：file-extension 未显式配置、namespace 下配置缺失 |

## 5. 风险与治理建议（面向阶段 1）

- 配置加载策略统一：明确“启动阶段必须拉取配置”的机制（`spring.config.import` + fail-fast/import-check），并在各服务一致化。
- 环境差异项收敛：将 `server-addr`、`namespace`、外部依赖地址（Redis/ES/RabbitMQ 等）从本地文件迁移至 Nacos（本地仅保留 `spring.application.name`、profile、以及 Nacos 连接信息的最小集合）。
- 敏感配置治理：对第三方密钥/Token/AK/SK 增加最小权限（命名空间隔离 + Nacos 权限）、脱敏审计、灰度与回滚机制；严禁在仓库与日志中出现明文。

