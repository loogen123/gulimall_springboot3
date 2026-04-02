# GuliMail 分布式电商项目
> 基于 Spring Cloud Alibaba + Clean Architecture 打造的生产级微服务电商基座。

GuliMail 是一套完整的分布式电商解决方案，采用最新的 Spring Cloud Alibaba 生态与 Clean Architecture（分层架构）理念重构。项目涵盖从商品检索、订单处理到 AI 智能客服的全链路业务。核心价值在于通过严格的“应用-领域-基础设施”分层，实现业务逻辑与技术实现的彻底解耦，并内置全自动化的质量门禁（PMD/SpotBugs）与性能守护测试，为企业级高并发、易维护系统的构建提供最佳实践范本。

## 🚀 功能特性
| 模块名称 | 核心功能 | 状态 |
| :--- | :--- | :--- |
| `gulimail-product` | 商品发布、SKU/SPU 详情、分类管理、缓存一致性（Canal/Redisson） | ✅ |
| `gulimail-order` | 下单幂等（Token 机制）、订单状态机、分布式事务（Seata/可靠消息） | ✅ |
| `gulimail-ware` | 库存自动锁库/释放、延时队列库存自动解锁、SKU 预警 | ✅ |
| `gulimail-cart` | 离线/在线购物车合并、价格试算、异步更新 | ✅ |
| `gulimail-auth-server` | 社交登录（GitHub）、JWT 令牌发放、Spring Session 共享 | ✅ |
| `gulimail-search` | 基于 Elasticsearch 的商品检索、聚合分析、高亮显示 | ✅ |
| `gulimail-seckill` | 令牌桶限流、秒杀库存预热、秒杀风控校验 | ✅ |
| `gulimail-ai` | AI 导购、SSE 流式对话、基于 LangChain4j 的意图识别 | ✅ |
| `gulimail-gateway` | 动态路由、分布式链路追踪（Micrometer）、全链路日志脱敏 | ✅ |
| `gulimail-member` | 用户画像、积分激励体系、收货地址管理 | ✅ |
| `gulimail-coupon` | 优惠券发放、满减/折扣/叠加逻辑校验 | ✅ |
| `gulimail-third-party` | 阿里云 OSS 存储、短信网关适配、邮件服务 | ✅ |

---

## 🏗️ 核心模块说明

### 🛒 gulimail-product (商品服务)
- **职责**：核心商品域模型维护，包括分类树构建、规格参数聚合及 SKU 详情检索。
- **目录**：`gulimail-product/`
- **核心类**：
  - `SkuItemApplicationService`: 编排 SKU 详情页所需的多维度数据。
  - `CategoryController`: 提供商品分类的树形结构查询。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-24%2F24-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 📦 gulimail-order (订单服务)
- **职责**：处理下单、支付回调、取消订单等核心链路，保证分布式环境下的一致性与幂等。
- **目录**：`gulimail-order/`
- **核心类**：
  - `SubmitOrderApplicationService`: 负责下单全流程编排（验价、锁库、创建订单）。
  - `OrderApiController`: 暴露标准的 RESTful 订单操作接口。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-34%2F34-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🏭 gulimail-ware (仓储服务)
- **职责**：精细化管理商品库存，支持分布式锁库与异步释放机制。
- **目录**：`gulimail-ware/`
- **核心类**：
  - `OrderLockStockApplicationService`: 执行远程锁库逻辑，处理库存回滚。
  - `WareSkuController`: 提供库存查询与调度接口。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-28%2F28-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🤖 gulimail-ai (AI 智能客服)
- **职责**：利用大模型能力提供智能化导购与售后支持，支持自然语言交互。
- **目录**：`gulimail-ai/`
- **核心类**：
  - `AiChatApplicationService`: 处理流式会话生命周期与意图识别。
  - `AiChatController`: 基于 SSE (Server-Sent Events) 的对话接口。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-19%2F19-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🔍 gulimail-search (检索服务)
- **职责**：基于 Elasticsearch 提供高性能的商品搜索与聚合过滤。
- **目录**：`gulimail-search/`
- **核心类**：
  - `SearchApplicationService`: 封装 DSL 查询逻辑，支持多维度复合检索。
  - `SearchController`: 接收前端检索请求并返回标准 DTO。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-19%2F19-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🛡️ gulimail-gateway (API 网关)
- **职责**：统一流量入口，执行跨域处理、灰度发布、限流及 TraceId 注入。
- **目录**：`gulimail-gateway/`
- **核心类**：
  - `TraceIdFilter`: 实现全局请求链路 ID 注入与响应头回显。
  - `SentinelGatewayConfig`: 配置网关层级熔断与流控规则。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-19%2F19-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🛒 gulimail-cart (购物车服务)
- **职责**：管理用户购物车数据，支持匿名购物车与登录购物车合并。
- **目录**：`gulimail-cart/`
- **核心类**：
  - `CartSelectionApplicationService`: 处理购物车选中状态与价格计算。
  - `CartController`: 提供购物车增删改查 REST 接口。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-25%2F25-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🔑 gulimail-auth-server (认证服务)
- **职责**：统一身份认证与授权，集成社交登录与单点登录（SSO）能力。
- **目录**：`gulimail-auth-server/`
- **核心类**：
  - `LoginApplicationService`: 处理账号密码登录与验证码校验。
  - `GithubOAuthApplicationService`: 适配 GitHub 第三方社交登录。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-22%2F22-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### ⚡ gulimail-seckill (秒杀服务)
- **职责**：承载高并发秒杀流量，通过库存预热与令牌机制保护后端服务。
- **目录**：`gulimail-seckill/`
- **核心类**：
  - `SeckillSkuApplicationService`: 秒杀资格校验与下单令牌发放。
  - `SeckillController`: 秒杀商品查询与抢购入口。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-19%2F19-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

### 🛠️ gulimail-common (公共基座)
- **职责**：沉淀通用的工具类、配置类、异常处理逻辑及自动装配组件。
- **目录**：`gulimail-common/`
- **核心类**：
  - `CommonMybatisAutoConfiguration`: 统一分页插件与公共字段填充配置。
  - `GulimailExceptionControllerAdvice`: 全局统一异常拦截与响应映射。
- **质量指标**：![Coverage](https://img.shields.io/badge/Tests-27%2F27-brightgreen) ![Quality](https://img.shields.io/badge/QualityGate-Passed-brightgreen)

---

## 🛠️ 技术栈
- **核心框架**：Spring Boot 3.x, Spring Cloud 2022, Spring Cloud Alibaba 2022
- **持久层**：MyBatis Plus 3.5, MySQL 8.0, Redis 7.0
- **中间件**：Nacos (注册/配置), RabbitMQ (消息), Elasticsearch 7.x, Seata (事务), Sentinel (限流)
- **AI 增强**：LangChain4j 0.33.0
- **质量保障**：JUnit 5, Mockito, PMD, SpotBugs, Micrometer Tracing

## 📖 开发与部署
1. **环境准备**：确保已安装 JDK 17+、Maven 3.8+、Docker 环境。
2. **敏感配置**：本项目敏感信息存放在 `../secure/application-secure.yml`，请联系管理员获取。
3. **快速启动**：
   ```bash
   mvn clean install -DskipTests
   docker-compose up -d # 启动中间件
   # 按需启动各微服务模块
   ```
4. **质量门禁**：
   ```bash
   mvn -Pquality-gate-strict verify # 执行严格静态检查与测试
   ```

## 📄 文档索引
- [AI 客服技术方案](./docs/tech-design/AI%E5%AE%A2%E6%9C%8D%E9%9C%80%E6%B1%82%E6%8A%80%E6%9C%AF%E6%96%B9%E6%A1%88%E8%AF%B4%E6%98%8E%E4%B9%A6.md)
- [OpenAPI 规范](./docs/tech-design/openapi.json)
- [重构进度与进度报告](./docs/refactor/progress.md)
