# AI 客服需求技术方案说明书

## 1. 接口设计

### 1.1 核心接口清单
遵循 RESTful 设计原则，所有接口均版本化（v1）。

| 接口名称 | URL | 方法 | 职责说明 | 鉴权/限流 |
| :--- | :--- | :--- | :--- | :--- |
| **流式对话** | `/api/ai/v1/chat/stream` | `GET` | 核心 AI 对话接口，返回 SSE 流 | JWT + Sentinel (10 QPS/User) |
| **会话历史** | `/api/ai/v1/sessions` | `GET` | 获取当前用户的历史会话列表 | JWT |
| **消息详情** | `/api/ai/v1/sessions/{id}/messages` | `GET` | 获取指定会话的所有消息详情 | JWT |
| **服务健康检查** | `/api/ai/v1/health` | `GET` | 检查 AI 模块及 LLM 连通性 | 无 / 无 |

### 1.2 接口规范
*   **请求格式**：`application/json` (除 SSE 接口外)。
*   **响应格式**：`application/json` 或 `text/event-stream`。
*   **错误码定义**：
    *   `200`: 成功
    *   `401`: 未登录/Token 过期
    *   `403`: 权限不足
    *   `429`: 请求过于频繁 (Sentinel 限流)
    *   `500`: 系统内部错误/LLM 响应异常

### 1.3 版本控制策略
采用 **路径隔离** 方式，如 `/api/ai/v1/...`。
*   `v1`: 当前稳定版本。
*   `v2`: 涉及破坏性变更（如数据模型重构）时开启，老接口通过网关转发至兼容逻辑。

---

## 2. 数据模型决策

### 2.1 评估与建表
基于审计与 3 年存储要求，必须对对话内容进行落库。

#### **表 1：AI 会话表 (ai_chat_session)**
用于管理多轮对话的生命周期。
| 字段名 | 类型 | 约束 | 索引 | 注释 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PK, AUTO_INC | - | 会话唯一标识 |
| `user_id` | `BIGINT` | NOT NULL | INDEX | 所属用户 ID |
| `title` | `VARCHAR(255)`| - | - | 会话标题（由 AI 生成或取第一句话） |
| `create_time` | `DATETIME` | NOT NULL | - | 创建时间 |
| `update_time` | `DATETIME` | - | - | 最后活动时间 |
| `is_deleted` | `TINYINT` | DEFAULT 0 | - | 逻辑删除标记 |

#### **表 2：AI 消息表 (ai_chat_message)**
存储具体的对话内容。
| 字段名 | 类型 | 约束 | 索引 | 注释 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PK, AUTO_INC | - | 消息 ID |
| `session_id` | `BIGINT` | NOT NULL | INDEX | 关联的会话 ID |
| `role` | `VARCHAR(20)` | NOT NULL | - | 角色: user/assistant/tool |
| `content` | `TEXT` | NOT NULL | - | 消息内容 |
| `token_usage` | `INT` | - | - | 消耗的 Token 数量 |
| `create_time` | `DATETIME` | NOT NULL | - | 发送时间 |

### 2.2 选型理由
*   **逻辑删除**：符合项目 `renren-fast` 及其他模块的惯例。
*   **乐观锁**：本场景并发写同一条记录概率极低，暂不使用。
*   **分库分表**：初始阶段 QPS 低，单表可支撑千万级数据，暂不实施，预留 `user_id` 作为分片键。

---

## 3. 技术选型与约束

### 3.1 开发规范
*   **包结构**：严格遵循 `com.lg.gulimail.ai` 下的 `controller`, `service`, `mapper`, `model.entity`, `model.dto`, `model.vo`。
*   **异常体系**：继承 `com.lg.common.exception.BizCodeEnum`，自定义 `AiBizException`。
*   **依赖版本**：
    *   Spring Boot: 2.x (对齐主项目)
    *   LangChain4j: 0.25.0
    *   MyBatis-Plus: 3.x (对齐 `product` 模块)

### 3.2 质量阈值
*   **Sonar 规范**：Major 以上缺陷 0。
*   **代码风格**：必须通过 `google-java-format` 或项目自带 Checkstyle。

---

## 4. 最小化实现步骤

| 迭代任务 | 改动范围 | 预计工时 | 风险点 | 回滚方案 |
| :--- | :--- | :--- | :--- | :--- |
| **T1: 基础框架与持久化** | 添加 MyBatis-Plus, 创建数据库表及 Entity/Mapper | 0.5d | 数据库权限 | 撤回 SQL 脚本 |
| **T2: V1 接口与流式优化** | 重构 AiChatController 为 v1，集成会话保存逻辑 | 1.0d | SSE 断连处理 | 切换回原有 Controller |
| **T3: 工具调用准确性调优** | 优化 Prompt，完善 ProductAiTools 的异常处理 | 1.0d | LLM 幻觉 | 恢复旧版 Prompt |
| **T4: 安全与限流集成** | 网关 JWT 校验配置，Sentinel 限流策略应用 | 0.5d | 误拦截正常用户 | 关闭网关限流插件 |
| **T5: 测试与交付** | 编写 JUnit5 单元测试与集成测试 | 1.0d | 外部 API 连通性 | - |

---

## 5. 测试与验收标准

### 5.1 自动化测试
*   **单元测试**：使用 Mockito 模拟 `OpenAiStreamingChatModel`，验证 `MallAssistant` 的消息组装逻辑。
*   **集成测试**：使用 `@SpringBootTest` 配合 H2 数据库验证消息持久化流程。

### 5.2 交付流水线
1.  `mvn checkstyle:check`
2.  `mvn test` (单元测试覆盖率需 >80%)
3.  `docker build` 镜像上传至私有仓库。

---

## 6. 文档与交付
*   **OpenAPI 规范**：见同目录下 `openapi.json`。
*   **README 更新**：在 `gulimail-ai/README.md` 中同步更新接口清单。
