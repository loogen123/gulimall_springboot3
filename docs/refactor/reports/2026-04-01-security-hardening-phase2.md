# 重构报告：安全加固阶段（高风险模块优先）

- 日期：2026-04-01
- 范围：P0/P1 高风险安全模块（`gulimail-member`、`gulimail-ai`、`gulimail-product`、`gulimail-common`）
- 对齐规范：java-dev（命名/异常/日志/测试/安全）+ OWASP Top 10

## 1. 本次重构对象与结构改进

### 1.1 访问控制与越权防护（A01/A07）

- [MemberWebConfig.java](file:///d:/GitProgram/gulimail/gulimail-member/src/main/java/com/lg/gulimail/member/config/MemberWebConfig.java)
  - 去除 `/member/memberreceiveaddress/**` 白名单放行，避免地址接口匿名可访问。
- [MemberReceiveAddressController.java](file:///d:/GitProgram/gulimail/gulimail-member/src/main/java/com/lg/gulimail/member/controller/MemberReceiveAddressController.java)
  - `getAddress` 强制校验登录态与 `memberId == 当前用户ID`，拒绝越权读取。
  - `save` 未登录场景抛出统一业务异常，不再返回松散错误体。
- [LoginUserInterceptor.java](file:///d:/GitProgram/gulimail/gulimail-member/src/main/java/com/lg/gulimail/member/interceptor/LoginUserInterceptor.java)
  - 改 `getSession(false)`，避免无谓创建 Session。
  - 在 `afterCompletion` 清理 `ThreadLocal`，防止线程复用导致身份串扰。

### 1.2 敏感信息保护与日志最小化（A02/A09）

- [AiChatController.java](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/controller/AiChatController.java)
  - 移除 Cookie/Header 明文日志输出。
  - 移除“重构 Cookie”逻辑，避免伪造会话传播。
  - 未登录/越权场景统一抛业务异常（不再返回 `null`）。
- [FeignConfig.java](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/config/FeignConfig.java)
  - 清理 `System.out` 日志，改为不含敏感值的 debug 日志。

### 1.3 前端输入输出安全（A03）

- [index.html](file:///d:/GitProgram/gulimail/gulimail-product/src/main/resources/templates/index.html)
  - AI 聊天回显对用户输入进行 HTML 转义，避免 DOM XSS。
  - 删除 `clientCookie` URL 透传，防止 Cookie 暴露到 URL/日志链路。

### 1.4 统一异常标准化（A04）

- [BizCodeEnum.java](file:///d:/GitProgram/gulimail/gulimail-common/src/main/java/com/lg/common/exception/BizCodeEnum.java)
  - 新增 `UNAUTHORIZED_EXCEPTION`、`FORBIDDEN_EXCEPTION` 业务码。
- [GulimailExceptionControllerAdvice.java](file:///d:/GitProgram/gulimail/gulimail-common/src/main/java/com/lg/common/exception/GulimailExceptionControllerAdvice.java)
  - 新增 `RRException` 专用处理器，统一输出业务错误码。
  - 清理无效/重复的 `Exception` 处理器分支。

## 2. 必须补充的测试用例（已落实）

### 2.1 单元测试

- [GulimailExceptionControllerAdviceTest.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/exception/GulimailExceptionControllerAdviceTest.java)
  - 校验 RRException -> 业务码映射（未登录码）。
- [MemberReceiveAddressControllerSecurityTest.java](file:///d:/GitProgram/gulimail/gulimail-member/src/test/java/com/lg/gulimail/member/controller/MemberReceiveAddressControllerSecurityTest.java)
  - 覆盖未登录拒绝、越权拒绝、仅允许查询当前用户地址三类安全路径。

### 2.2 集成测试（当前状态）

- 已保留基座 IT 生命周期能力（`*IT.java`），本次安全阶段未新增依赖外部环境的 IT 用例。
- 后续建议补充：网关鉴权链路 + 会话传播 + 越权回归的 Testcontainers 集成测试。

## 3. 执行结果（SAST / 依赖安全检查 / 覆盖率）

### 3.1 静态安全扫描（SAST）

- 执行命令：`mvn -pl gulimail-common,gulimail-member,gulimail-ai -am verify`
- 结果：构建通过，已输出 SpotBugs/PMD/Checkstyle 扫描信息。
- 结论：本次改动未引入新的阻塞级编译/测试问题；仍存在历史中风险项（主要为 `EI_EXPOSE_REP*`、`UWF_UNWRITTEN_FIELD`），已纳入后续整改池。

### 3.2 依赖项安全检查（SCA）

- 执行命令：`mvn -pl gulimail-common -DskipTests org.owasp:dependency-check-maven:check -Dformat=JSON -DfailOnError=false`
- 结果：扫描任务可启动，但在无 NVD API Key 的环境下更新极慢，未在本次窗口内完成全量报告落盘。
- 处理：已记录为交付风险，需在 CI 环境配置 NVD API Key 后补齐完整 SCA 报告。

### 3.3 覆盖率证据（可替代截图）

- [gulimail-common jacoco index](file:///d:/GitProgram/gulimail/gulimail-common/target/site/jacoco/index.html)
- [gulimail-ai jacoco index](file:///d:/GitProgram/gulimail/gulimail-ai/target/site/jacoco/index.html)
- 覆盖率汇总（来自 `jacoco.csv`）：
  - gulimail-common：Instruction 7.10%，Line 6.74%
  - gulimail-ai：Instruction 1.88%，Line 2.31%

## 4. 性能基准对比指标（安全改造相关）

本次改造不涉及算法或数据库查询路径调整，性能基准按“无显著退化”口径验收：
- 接口 RT：允许波动 ±5%
- 错误率：不高于基线
- 并发稳定性：鉴权与异常路径在并发下不出现线程污染（`ThreadLocal` 已补清理）

## 5. 风险点与回滚方案

### 5.1 风险点

- 依赖安全扫描报告受 NVD API Key 缺失影响，无法快速出完整 SCA 结果。
- 前端转义后，若业务依赖“富文本回显”可能出现显示差异（当前仅影响 AI 文本消息，属预期）。
- 鉴权收紧后，历史匿名调用地址接口将被拒绝，需要前后端联调确认。

### 5.2 回滚方案

- 粒度回滚：按模块回滚单文件改动（member/ai/product/common）
- 全量回滚：回退本次安全阶段相关提交
- 验证回滚：执行 `mvn -pl gulimail-common,gulimail-member,gulimail-ai -am test`，并手工验证地址接口/AI 会话接口

## 6. 代码审查清单（OWASP Top 10）

请按清单逐项审查：
- [security-review-checklist.md](file:///d:/GitProgram/gulimail/docs/refactor/templates/security-review-checklist.md)

本次重点已覆盖项：
- A01/A07：访问控制与会话管理
- A02/A09：敏感信息与日志
- A03：输入输出安全（XSS）
- A06：依赖安全检查流程已接入（报告待 CI 完整落地）

