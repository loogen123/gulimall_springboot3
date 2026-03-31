# GuliMail 分布式电商项目

本项目是一个基于 Spring Cloud Alibaba 的大型分布式电商系统。

## 核心模块
*   `gulimail-product`: 商品服务
*   `gulimail-order`: 订单服务
*   `gulimail-ai`: 智能客服服务 (New)
*   ...

## AI 客服相关文档
*   [AI 客服需求分析文档（技术可行性优化版）.md](./AI%E5%AE%A2%E6%9C%8D%E9%9C%80%E6%B1%82%E5%88%86%E6%9E%90%E6%96%87%E6%A1%A3%EF%BC%88%E6%8A%80%E6%9C%AF%E5%8F%AF%E8%A1%8C%E6%80%A7%E4%BC%98%E5%8C%96%E7%89%88%EF%BC%89.md)
*   [AI 客服需求技术方案说明书.md](./docs/tech-design/AI%E5%AE%A2%E6%9C%8D%E9%9C%80%E6%B1%82%E6%8A%80%E6%9C%AF%E6%96%B9%E6%A1%88%E8%AF%B4%E6%98%8E%E4%B9%A6.md)
*   [OpenAPI 规范](./docs/tech-design/openapi.json)

## 开发规范
见 `java-dev/` 目录下的相关说明。

## 敏感数据管理
本项目遵循以下敏感数据管理规则：
1. **配置文件**: 包含内网IP、数据库账号、API密钥等敏感信息的 `application-secure.yml` 配置文件统一存放在项目根目录外的 `../secure/` 目录中。
2. **本地获取**: 开发人员需从内部渠道获取该文件并按上述路径放置，以免在 Git 提交中泄露敏感信息。
3. **环境变量**: 对于生产环境，敏感配置应通过 Kubernetes ConfigMap 或 Nacos 动态配置加载。
