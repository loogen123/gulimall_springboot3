# Gulimail AI 模块

基于 LangChain4j 的智能客服微服务。

## 1. 核心功能
*   **智能导购**：支持商品查询、库存与秒杀状态校验。
*   **售后查询**：支持用户订单列表及状态查询。
*   **流式对话**：基于 SSE 实现低延迟打字机效果。

## 2. 技术栈
*   **Spring Boot 3.x**
*   **LangChain4j 0.33.0**（OpenAI 兼容模型接入）
*   **Spring Session + Redis**
*   **MyBatis-Plus 3.5.5**

## 3. 接口清单 (v1)

| 接口 | 方法 | URL | 描述 |
| :--- | :--- | :--- | :--- |
| **流式聊天** | `GET` | `/api/ai/v1/chat/stream` | 返回 SSE 流 |
| **会话历史** | `GET` | `/api/ai/v1/sessions` | 获取当前用户的历史会话 |
| **会话消息** | `GET` | `/api/ai/v1/sessions/{id}/messages` | 获取指定会话消息列表 |
| **健康检查** | `GET` | `/api/ai/v1/health` | 检查 AI 服务连通性 |

更多详细设计见：[AI 客服需求技术方案说明书.md](../docs/tech-design/AI%E5%AE%A2%E6%9C%8D%E9%9C%80%E6%B1%82%E6%8A%80%E6%9C%AF%E6%96%B9%E6%A1%88%E8%AF%B4%E6%98%8E%E4%B9%A6.md)
