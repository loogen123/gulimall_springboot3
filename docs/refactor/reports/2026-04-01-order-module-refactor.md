# 重构报告：gulimail-order（阶段推进）

- 日期：2026-04-01
- 模块：`gulimail-order`
- 对齐文档：`phase-plan.md`、`steps-and-estimates.md`、`java-dev/SKILL.md`、`java-style.md`

## 1. 重构目标与范围

- 在不改变对外契约的前提下，提升订单模块登录拦截链路的安全性与代码可维护性。
- 修复控制层宽泛异常吞噬问题，符合“Controller 不吞异常”的规范。
- 优化订单查询的 N+1 性能问题，减少数据库往返。
- 补充关键行为路径的单元测试，提升回归稳定性。

## 2. 代码变更明细

### 2.1 拦截器安全收敛与可维护性提升

- [LoginUserInterceptor.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/interceptor/LoginUserInterceptor.java)
  - 支付回调放行由宽松匹配调整为固定路径集合匹配。
  - 登录态读取改为 `getSession(false)`，避免无意义会话创建。
  - 统一日志输出方式，移除 `System.out` 调试语句。
  - 保留并验证 `afterCompletion` 的 `ThreadLocal` 清理行为。

### 2.2 控制器异常处理规范化

- [OrderController.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/controller/OrderController.java)
  - `listWithItem` 移除 `catch (Exception)`。
  - 显式处理未登录分支并返回 401，避免吞异常掩盖真实问题。

### 2.3 配置重复注册清理

- 删除 [OrderWebConfig.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/OrderWebConfig.java)
  - 该类与 [OrderWebConfiguration.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/OrderWebConfiguration.java) 重复注册同一拦截器。
  - 清理后只保留一套拦截器配置，避免行为叠加。

### 2.4 性能优化（N+1 查询消除）

- [OrderServiceImpl.java](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/service/impl/OrderServiceImpl.java)
  - `queryPageWithItem` 从“每个订单单独查一次订单项”改为“按 `order_sn` 批量查询并按订单号分组回填”。
  - 在空记录场景提前返回，减少无效处理。

## 3. 测试覆盖补充

- 新增 [OrderControllerTest.java](file:///d:/GitProgram/gulimail/gulimail-order/src/test/java/com/lg/gulimail/order/controller/OrderControllerTest.java)
  - 覆盖未登录 401 与已登录成功返回分页结果。
- 新增 [LoginUserInterceptorTest.java](file:///d:/GitProgram/gulimail/gulimail-order/src/test/java/com/lg/gulimail/order/interceptor/LoginUserInterceptorTest.java)
  - 覆盖登录放行、支付回调放行、未登录 401、未登录重定向、请求后清理 ThreadLocal。

## 4. 验证结果

- 模块测试：`mvn test -pl gulimail-order` 通过。
- 全量测试：根聚合 `mvn test` 通过，Reactor 全模块成功。

## 5. 技术债务与后续建议

- `OrderController` 其余通用 CRUD 接口仍以后台管理口径实现，建议在后续阶段补齐权限模型区分（管理端与用户端）。
- `RabbitTest` 依赖外部中间件环境，建议后续引入可隔离测试配置以降低环境耦合。

## 6. 代码审查建议

- 审查入口：
  - [security-review-checklist.md](file:///d:/GitProgram/gulimail/docs/refactor/templates/security-review-checklist.md)
  - [mr-template.md](file:///d:/GitProgram/gulimail/docs/refactor/templates/mr-template.md)
- 本次审查重点：
  - 支付回调白名单是否最小化且不影响回调可用性。
  - `queryPageWithItem` 批量查询逻辑在分页场景下的结果一致性。
  - ThreadLocal 清理是否覆盖全部请求路径。
