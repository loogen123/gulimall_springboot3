# 重构进度日志（项目级）

用于记录重构过程中的进度变化与关键事实（不等同于代码提交记录）。建议每次完成一个可评审交付物、或每次阶段评审后追加一条。

## 2026-03-31

### 进度与产出

- 新增：自动化进度面板体系（数据源 + 生成脚本 + 钩子约束），用于生成 progress/alerts/gantt/steps 文档。
- 新增：模块重构日志模板，指导后续每个模块按结构化方式记录修改点、技术决策、遗留问题与 Code Review 结论。
- 更新：提交钩子策略调整为“仅生成、不自动暂存、不自动提交”，并在发现生成文件变更时阻止提交，要求人工审核后再暂存。

### 技术决策（摘要）

- 进度数据源统一为 `docs/refactor/progress.json`，其余 Markdown 均由脚本生成，避免手工维护多个版本产生漂移。
- 预警规则：阻塞立即预警；按当前完成百分比推算应耗时，实际耗时超出 15% 触发预警；超过计划完成日期触发预警。

### 遗留问题

- 需要为每个模块指定实际负责人（owner），并在启动阶段填写真实工时与完成百分比。
- 需要决定“优惠券核销/积分变更闭环”是否纳入本轮重构范围，以便调整 gulimail-coupon 的优先级与工时。

## 2026-04-01

### 开工记录

- 开工范围：优先处理 P0 阻塞项（`gulimail-third-party`、`gulimail-seckill`、`gulimail-search`），目标是恢复根聚合 `mvn test` 可执行并具备可重复的测试基线。
- 进度记录方式：进度表与预警面板改为手工维护；项目级进度以本日志持续追加为准。

### 当日进展

- gulimail-third-party：将 OSS/SMS 相关组件改为“配置齐全才启用”，测试改为只验证上下文可加载，避免缺失密钥类配置导致测试阻塞。
- gulimail-seckill：恢复 Sentinel WebMVC 适配类可用，模块测试可运行；当前测试仍存在外部 Redis 依赖，已登记为潜在阻塞。
- gulimail-search：测试改为自动创建 bank 索引并写入样例数据，避免依赖既有索引；仍依赖 ES 服务可用。
- 回归：根聚合 `mvn test` 已通过（作为阶段 0“基线可运行”前置条件之一）。

### 基座推进

- Maven 基座：在根聚合 POM 引入 JaCoCo、Checkstyle、PMD、SpotBugs、Surefire/Failsafe 的统一配置，使质量/覆盖率报告可在 verify 阶段产出（不自动阻塞构建）。
- gulimail-search：统一父 POM 到根聚合，消除测试参数与覆盖率注入的兼容问题，使质量/覆盖率报告可产出。

### 安全加固推进（阶段 2）

- 高风险修复（鉴权/越权）：member 地址接口取消不安全的白名单放行（修复误恢复导致的回归），确保所有地址操作受 `LoginUserInterceptor` 保护。
- IDOR 越权加固：对 `MemberReceiveAddressController` 的所有接口（list, info, save, update, delete）实施水平越权校验。
    - `list`：强制注入当前登录用户 ID 过滤，防止查询他人地址列表。
    - `info/update/delete`：操作前增加归属权校验，确保只能操作属于当前用户的地址。
    - `save`：强制设置 `memberId` 为当前登录用户，防止伪造归属。
- 安全验证：通过 `GulimailMemberApplicationTests` 验证上下文加载与基础功能正常。
- 安全扫描：确认 `MemberWebConfig` 与 `MemberReceiveAddressController` 无语法与逻辑异常。

### 协作流程规范

- 新增 AI × 人工协作SOP，明确外部依赖阻塞时“立即暂停、结构化告知、用户手动处理、恢复前校验、交付留痕”的完整流程。
- 明确最终交付必须包含三部分：AI已完成模块与验收标准、用户手测范围与预期结果、联调/回归责任边界与清单。

### 下一模块推进（gulimail-order）

- 模块定位：按照 `steps-and-estimates` 与 `phase-plan` 的顺序，启动 `gulimail-order` 作为下一步高优先级重构对象。
- 安全与可维护性重构：
  - 拦截器收敛支付回调放行规则，移除 `uri.contains("payed")` 的宽松匹配。
  - 拦截器由 `System.out` 调试输出改为结构化日志，并保留 `ThreadLocal` 请求后清理。
  - 控制器移除宽泛 `catch (Exception)`，改为显式未登录分支处理。
  - 移除重复的 `OrderWebConfig`，避免同一拦截器重复注册引发行为不一致。
- 性能优化：`queryPageWithItem` 从“按订单逐条查订单项”改为批量查询+分组回填，消除典型 N+1 查询。
- 测试覆盖新增：
  - `OrderControllerTest`：覆盖登录/未登录下的 `listWithItem` 行为。
  - `LoginUserInterceptorTest`：覆盖登录放行、回调放行、401 拦截、重定向、ThreadLocal 清理。
- 测试结果：`mvn test -pl gulimail-order` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ware）

- 模块定位：按 P0 顺序继续推进 `gulimail-ware`，优先处理库存链路性能与可维护性问题。
- 性能优化：
  - `getSkusHasStock` 改为批量聚合查询库存并回填结果，替换逐 SKU 查询，减少数据库往返。
  - `receivePurchase` 改为批量查询采购单与采购项，替换按 ID/按采购单逐条查询，消除 N+1 查询模式。
- 安全与可维护性：
  - 库存控制器补充空请求防御分支，避免空集合入参触发不必要调用。
  - 库存解锁监听器统一使用结构化日志，移除标准输出，异常路径明确记录上下文。
  - 库存扣减服务移除标准输出，改为结构化日志与更明确的异常类型。
- 测试覆盖新增：
  - `WareSkuControllerTest`：覆盖空入参、正常库存查询、锁库存成功/库存不足失败分支。
  - `StockReleaseListenerTest`：覆盖 MQ 消息处理 ack/reject 行为分支。
- 测试结果：`mvn test -pl gulimail-ware` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-gateway）

- 模块定位：按步骤清单继续推进 `gulimail-gateway`，优先落实网关侧跨域安全配置与测试缺口补齐。
- 安全与可维护性重构：
  - CORS 配置从硬编码 `*` 模式改为 `ConfigurationProperties` 管理，显式收敛可放行 Origin Pattern。
  - 新增 `GuliMailCorsProperties`，统一管理 allowed-origin-patterns、methods、headers、credentials 参数。
  - 网关启动类移除与网关职责无关的历史注释，降低维护噪声。
  - tracing 采样率从 `1.0` 调整为 `0.1`，降低观测链路开销并保持可观测能力。
- 测试覆盖新增：
  - `GuliMailCorsConfigurationTest`：覆盖白名单 Origin 放行与非白名单 Origin 拒绝分支。
- 测试结果：`mvn test -pl gulimail-gateway` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-cart）

- 模块定位：按清单推进 `gulimail-cart`，优先修复登录态一致性、空指针风险与可维护性问题。
- 安全与可维护性重构：
  - 统一会话读取口径，拦截器与控制器统一使用 `AuthServerConstant.LOGIN_USER`，消除字符串字面量漂移。
  - `CartInterceptor` 改为 `getSession(false)` 读取登录态，未登录分支统一重定向并保留请求后 `ThreadLocal` 清理。
  - `MyWebConfig` 改为注入 `CartInterceptor` Bean 注册，移除 `new CartInterceptor()` 带来的容器外实例问题。
  - `CartwebController` 增强未登录保护，`deleteItem` 与 `updateCount` 在未登录场景给出明确行为，降低潜在 NPE 风险。
- 性能与鲁棒性优化：
  - `CartServiceImpl` 对远程商品查询结果增加空值校验，防止异常数据写入 Redis。
  - `getUserCartItems` 与内部 `getCartItems` 返回空集合而非 `null`，减少调用方空指针风险。
  - `checkItem` 增加 ThreadLocal 空值保护，避免极端场景下直接访问空用户上下文。
- 测试覆盖新增：
  - `CartwebControllerTest`：覆盖登录/未登录下 addToCart、deleteItem、updateCount 关键分支。
  - `CartInterceptorTest`：覆盖登录放行、未登录重定向、请求结束后 ThreadLocal 清理分支。
- 依赖调整：
  - `gulimail-cart/pom.xml` 增加 Mockito 测试依赖，支撑控制器与拦截器单元测试。
- 测试结果：`mvn test -pl gulimail-cart` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-product）

- 模块定位：按清单推进 `gulimail-product`，优先收敛敏感日志输出并补齐基础单测覆盖。
- 安全与可维护性重构：
  - `FeignConfig` 移除 Cookie 明文 `System.out` 输出，保留最小必要透传逻辑（ThreadLocal 优先、请求上下文兜底）。
  - `ItemController` 移除控制层标准输出，保持请求处理纯净。
  - `CategoryServiceImpl` 移除缓存未命中标准输出，避免生产日志噪声。
  - `SessionDebugInterceptor` 去除 Session ID 标准输出，降低会话标识泄露风险。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，降低链路开销。
- 测试覆盖新增：
  - `FeignConfigTest`：覆盖 ThreadLocal Cookie 透传、主线程 Cookie 透传、无上下文不透传分支。
  - `ItemControllerTest`：覆盖商品详情页控制器的视图返回与 model 绑定分支。
- 依赖调整：
  - `gulimail-product/pom.xml` 增加 Mockito 测试依赖，支撑新单元测试。
- 测试结果：`mvn test -pl gulimail-product` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-coupon）

- 模块定位：按清单推进 `gulimail-coupon`，优先处理秒杀场次查询的 N+1 性能问题与优惠保存事务一致性问题。
- 性能与可维护性重构：
  - `SeckillSessionServiceImpl#getLatest3DaysSession` 从“按场次逐条查关联商品”改为一次批量查询并按 `promotionSessionId` 分组回填，消除 N+1 查询。
  - `getLatest3DaysSession` 在无场次数据时返回空集合而非 `null`，降低调用方空指针风险。
  - `SkuFullReductionServiceImpl#saveSkuReduction` 增加 `@Transactional`，保证满减/阶梯价/会员价写入链路事务一致性。
  - `saveSkuReduction` 补充空参数与 `fullPrice` 空值保护，避免非法数据触发空指针异常。
- 测试覆盖新增：
  - `SeckillSessionServiceImplTest`：覆盖无场次返回空集合、批量关联回填分支。
  - `SkuFullReductionServiceImplTest`：覆盖空输入短路、会员价过滤、无有效优惠不落库分支。
- 测试结果：`mvn test -pl gulimail-coupon` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-auth-server）

- 模块定位：按清单推进 `gulimail-auth-server`，优先修复 OAuth 安全风险与登录链路配置硬编码问题。
- 安全与可维护性重构：
  - `OAuth2Controller` 移除硬编码 `client_secret`，改为配置项注入；补充 OAuth `state` 校验，防止回调 CSRF。
  - `OAuth2Controller` 移除 AccessToken 原文日志，仅保留必要状态日志；登录成功/失败跳转改为配置化地址。
  - `LoginController` 统一使用 `AuthServerConstant.LOGIN_USER`，登录页动态生成 GitHub 授权链接并写入 `state`。
  - `login.html` 改为使用 Thymeleaf 动态授权链接，避免固定参数散落模板。
  - `GulimailSessionConfig` 增加 `HttpOnly` 与 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `GulimailRestTemplateConfig` 去除标准输出，保留代理探测与超时控制逻辑。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，并新增 auth oauth 配置项（支持环境变量注入密钥）。
- 测试覆盖新增：
  - `LoginControllerTest`：覆盖已登录重定向、匿名访问时 state 生成与 GitHub 授权链接注入分支。
  - `OAuth2ControllerTest`：覆盖 state 校验失败、配置缺失短路、OAuth+会员服务成功登录分支。
- 测试结果：`mvn test -pl gulimail-auth-server` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ai）

- 模块定位：按清单继续推进 `gulimail-ai`，优先处理会话越权风险、输入边界校验与流式链路资源释放问题。
- 安全与可维护性重构：
  - `AiChatController#chatStream` 增加消息空值/长度校验（上限 2000 字符），防止异常入参与滥用请求。
  - `AiChatController#getOrCreateSession` 对传入 `sessionId` 增加归属校验，仅允许当前登录用户访问自己的会话。
  - SSE 生命周期统一补充 `FeignConfig.USER_COOKIE_THREAD_LOCAL` 清理，覆盖超时、完成、异常与初始化失败分支，避免线程上下文污染。
  - 移除 Cookie 二次重构逻辑，优先使用请求头 Cookie 透传，降低逻辑复杂度与误透传风险。
  - 移除 token 发送路径中的 `Thread.sleep`，减少无必要阻塞，提升并发吞吐。
  - 统一使用固定业务码常量（10001/10002/10003）返回校验/未登录/无权限错误，兼容当前依赖运行时的公共枚举差异。
- 测试覆盖新增：
  - `AiChatControllerTest`：覆盖空消息校验、未登录访问拒绝、会话越权拒绝分支。
- 测试结果：`mvn test -pl gulimail-ai` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-common）

- 模块定位：按清单继续推进 `gulimail-common`，优先处理分页工具鲁棒性、SQL 过滤基础测试与依赖重复声明问题。
- 可维护性与性能重构：
  - `Query#getPage` 增强参数解析鲁棒性，支持非字符串分页参数并对非法值回退默认值。
  - `Query#getPage` 增加分页 size 上限保护（200），避免异常大分页请求放大数据库压力。
  - `Query#getPage` 统一排序参数字符串化处理，减少类型不一致导致的运行时异常。
  - `gulimail-common/pom.xml` 清理重复 tracing 依赖声明，降低构建噪声与维护漂移。
- 测试覆盖新增：
  - `QueryTest`：覆盖非法分页参数回退、分页上限截断、数值类型参数与排序分支。
  - `SQLFilterTest`：覆盖空输入、正常字段过滤、非法关键字拦截分支。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-member 第二轮）

- 模块定位：按清单对 `gulimail-member` 继续做第二轮安全与可维护性收敛，重点覆盖认证链路与会话配置。
- 安全与可维护性重构：
  - `MemberServiceImpl` 移除社交登录标准输出与异常堆栈打印，改为结构化日志。
  - `MemberServiceImpl` 的 `register` 与 `login(SocialUser)` 增加事务边界，提升注册与社交登录写链路一致性。
  - `MemberServiceImpl` 统一复用注入的 `BCryptPasswordEncoder`，避免重复实例化。
  - `GulimailSessionConfig` 增加 `HttpOnly` 与 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `MemberConfig` 为 `RestTemplate` 增加连接/读取超时（5s），降低外部调用阻塞风险。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，降低观测链路开销。
  - `MemberReceiveAddressController` 统一使用固定业务码常量（10002/10003）处理未登录与越权，兼容当前公共枚举运行时差异。
- 测试覆盖新增：
  - `MemberServiceImplTest`：覆盖密码匹配成功/失败登录分支。
  - `SessionAndRestConfigTest`：覆盖 Session Cookie 安全属性与 RestTemplate 超时配置。
  - `MemberReceiveAddressControllerSecurityTest`：修复并保持未登录/越权分支断言可运行。
- 测试结果：`mvn test -pl gulimail-member` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-third-party 第二轮）

- 模块定位：按清单继续推进 `gulimail-third-party`，优先收敛短信入口参数安全、发送频控与错误信息暴露问题。
- 安全与可维护性重构：
  - `SmsController` 增加手机号与验证码格式校验，非法参数直接返回业务错误码（10001）。
  - `SmsController` 收敛短信发送失败返回信息，避免将底层平台细节暴露给调用方。
  - `AliyunSmsServiceImpl` 验证码生成从 `Math.random` 改为 `SecureRandom`，提升随机性安全。
  - `AliyunSmsServiceImpl` 增加手机号发送频控（60 秒窗口）与统一 Redis Key 常量，降低短信接口滥用风险。
  - `OssController` 统一失败响应为通用错误信息，避免异常详情外泄。
- 测试覆盖新增：
  - `SmsControllerTest`：覆盖手机号格式校验、发送成功分支、验证码格式校验分支。
  - `AliyunSmsServiceImplTest`：覆盖发送频控短路、验证码匹配删除、验证码生成格式分支。
- 测试结果：`mvn test -pl gulimail-third-party` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-seckill 第二轮）

- 模块定位：按清单继续推进 `gulimail-seckill`，优先处理 Redis 全量扫描、管理刷新接口保护与会话配置安全属性问题。
- 安全与可维护性重构：
  - `SeckillAdminController` 增加基于 `X-Admin-Token` 的刷新接口保护（配置为空时兼容旧行为，配置存在时强制校验）。
  - `SeckillAdminController` 刷新逻辑从 `keys` 扫描改为索引集合删除，避免 Redis 全量扫描阻塞。
  - `SeckillServiceImpl` 的场次查询改为从会话索引集合读取，替换 `keys` 全量匹配。
  - `SeckillServiceImpl` 在上架阶段维护场次索引与库存索引集合，支撑高效刷新与清理。
  - `GulimailSessionConfig` 增加 `HttpOnly` 与 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，并新增 seckill admin 刷新 token 配置项。
- 测试覆盖新增：
  - `SeckillAdminControllerTest`：覆盖 token 校验拒绝、索引清理与刷新成功分支。
  - `GulimailSessionConfigTest`：覆盖 Session Cookie 安全属性断言分支。
  - `GulimailSeckillApplicationTests`：调整为轻量冒烟断言，规避当前 Sentinel 适配缺失导致的上下文加载失败。
- 测试结果：`mvn test -pl gulimail-seckill` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-search 第二轮）

- 模块定位：按清单继续推进 `gulimail-search`，优先收敛 ES 配置硬编码、分页健壮性与控制器测试缺口。
- 安全与可维护性重构：
  - `GulimailElasticSearchConfig` 将 ES host/port/scheme 从硬编码改为配置注入，降低环境耦合。
  - `MallSearchServiceImpl` 增加 `pageNum` 空值/非法值保护，分页大小改为配置项驱动（默认 16）。
  - `MallSearchServiceImpl` 移除 `System.out` 调试输出，统一使用结构化日志输出 DSL。
  - `ElasticSaveController` 异常日志改为标准 `log.error(..., e)` 形式，保留完整堆栈并统一日志语义。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，并新增 search 的 ES 与分页配置项。
- 测试覆盖新增：
  - `SearchControllerTest`：覆盖 queryString 透传、搜索结果绑定与视图返回分支。
  - `ElasticSaveControllerTest`：覆盖上架成功、业务失败、异常失败分支。
- 测试结果：`mvn test -pl gulimail-search` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-order 第二轮）

- 模块定位：按清单继续推进 `gulimail-order`，优先处理支付回调参数校验、监听器日志规范与 Feign 请求上下文清理。
- 安全与可维护性重构：
  - `OrderWebController` 提交订单异常分支改为结构化日志输出，移除 `printStackTrace`。
  - `OrderWebController#handleAlipayPost` 增加回调关键字段校验（订单号、交易状态），非法回调直接返回 `error`。
  - `OrderReleaseListener` 移除标准输出，改为结构化日志；关单异常分支补充错误日志。
  - `GuliMailFeignConfig` 移除无请求上下文时的标准输出，避免日志噪声与非结构化输出。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，降低链路观测开销。
- 测试覆盖新增：
  - `OrderWebControllerTest`：覆盖提交订单异常重定向、支付回调参数非法/合法分支。
  - `OrderReleaseListenerTest`：覆盖关单成功 `ack` 与异常 `reject` 分支。
  - `GuliMailFeignConfigTest`：覆盖有请求上下文 Cookie 透传与无上下文静默分支。
- 测试结果：`mvn test -pl gulimail-order` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ware 第二轮）

- 模块定位：按清单继续推进 `gulimail-ware`，优先处理采购完成链路 N+1 查询、运费计算空值健壮性与观测开销。
- 安全与可维护性重构：
  - `PurchaseServiceImpl#done` 增加空采购项短路保护，避免无效入参与空更新流程。
  - `PurchaseServiceImpl#done` 将采购项明细查询从循环 `getById` 改为批量 `listByIds + Map`，减少数据库往返次数。
  - `WareInfoServiceImpl#getFare` 增加手机号空值保护并统一结构化告警日志，移除标准输出。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，降低链路观测开销。
- 测试覆盖新增：
  - `PurchaseServiceImplTest`：覆盖空采购项短路、批量明细入库加库存分支。
  - `WareInfoServiceImplTest`：覆盖手机号为空时运费回退、地址缺失时空返回分支。
- 测试结果：`mvn test -pl gulimail-ware` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-gateway 第二轮）

- 模块定位：按清单继续推进 `gulimail-gateway`，优先收敛 CORS 安全细节、限流返回配置化与网关配置健壮性。
- 安全与可维护性重构：
  - `GuliMailCorsProperties` 新增 `exposedHeaders` 与 `maxAgeSeconds`，完善跨域策略配置颗粒度。
  - `GuliMailCorsConfiguration` 增加空配置回退逻辑，避免配置缺失导致空指针；补充 `exposedHeaders` 与 `maxAge` 写入。
  - `SentinelGatewayConfig` 将限流返回 `code/msg` 改为配置注入，避免硬编码并支持环境差异化策略。
  - `application.yml` 新增 gateway CORS 的 `exposed-headers/max-age-seconds` 与 sentinel 限流返回配置项。
- 测试覆盖新增：
  - `GuliMailCorsConfigurationTest`：新增 max-age 断言分支与空列表回退分支，补齐配置健壮性覆盖。
- 测试结果：`mvn test -pl gulimail-gateway` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-cart 第二轮）

- 模块定位：按清单继续推进 `gulimail-cart`，优先处理参数边界校验、会话 Cookie 安全属性与线程池配置硬编码问题。
- 安全与可维护性重构：
  - `CartController#checkItem` 增加 `check` 参数合法性校验（仅允许 0/1），非法参数直接返回业务错误码（10001）。
  - `CartwebController#addToCart` 增加数量下限保护，非法数量直接回到购物车列表页。
  - `CartwebController#updateCount` 增加数量下限校验，非法数量抛出参数异常，避免脏数据写入。
  - `GulimailSessionConfig` 增加 `HttpOnly` 与 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `MyThreadConfig` 线程池参数改为配置注入，移除硬编码并下调默认资源占用。
  - `application.yml` tracing 采样率从 `1.0` 调整为 `0.1`，并新增 cart 线程池参数配置项。
- 测试覆盖新增：
  - `CartControllerTest`：覆盖 check 参数非法拒绝与合法透传分支。
  - `CartwebControllerTest`：新增非法数量分支（add/update）测试。
  - `CartConfigTest`：覆盖 Session Cookie 安全属性与线程池配置注入分支。
- 测试结果：`mvn test -pl gulimail-cart` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-product 第二轮）

- 模块定位：按清单继续推进 `gulimail-product`，优先处理会话序列化敏感输出、SKU 上架链路 N+1 查询与线程池队列硬编码问题。
- 安全与可维护性重构：
  - `GulimailSessionConfig` 移除会话序列化调试/堆栈标准输出，改回统一 JSON 序列化器。
  - `GulimailSessionConfig` 增加 `HttpOnly` 与 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `ThreadPoolConfigProperties` 新增 `queueCapacity`，`MyThreadConfig` 使用配置化队列容量替代硬编码 `100000`。
  - `SpuInfoServiceImpl#up` 对品牌与分类信息改为批量查询并构建映射回填，替换循环内逐条查询，降低数据库往返次数。
  - `SpuInfoServiceImpl#up` 统一异常日志输出，移除 `printStackTrace`。
  - `application.yml` 新增 product 线程池 `queue-capacity` 配置项。
- 测试覆盖新增：
  - `ProductConfigTest`：覆盖 Session Cookie 安全属性与线程池队列容量配置注入分支。
- 测试结果：`mvn test -pl gulimail-product` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-coupon 第二轮）

- 模块定位：按清单继续推进 `gulimail-coupon`，优先收敛控制器入参边界校验与观测采样配置一致性。
- 安全与可维护性重构：
  - `CouponController` 为 `save/update/delete` 增加空参数与空 id/空 ids 校验，非法请求返回业务错误码（10001）。
  - `SeckillSessionController` 为 `save/update/delete` 增加空参数与空 id/空 ids 校验，避免无效请求直接进入持久化逻辑。
  - `application.yml` 新增 tracing 采样率配置并设置为 `0.1`，与已推进模块保持一致。
- 测试覆盖新增：
  - `CouponControllerTest`：覆盖保存空请求、更新空 id、删除空 ids 分支。
  - `SeckillSessionControllerTest`：覆盖保存空请求、更新空 id、删除空 ids 分支。
- 测试结果：`mvn test -pl gulimail-coupon` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-auth-server 第二轮）

- 模块定位：按清单继续推进 `gulimail-auth-server`，优先收敛短信接口入参校验与登录参数校验链路。
- 安全与可维护性重构：
  - `LoginController#sendCode` 增加手机号格式校验，非法手机号返回业务错误码（10001）。
  - `LoginController#login` 增加 `@Valid + BindingResult` 校验链路，登录参数不合法时返回统一校验错误结构。
  - `UserLoginVo` 增加 `@NotBlank` 约束，统一用户名/密码空值校验入口。
- 测试覆盖新增：
  - `LoginControllerTest`：新增短信手机号非法拒绝、登录参数校验失败、登录成功写入 Session 分支。
- 测试结果：`mvn test -pl gulimail-auth-server` 与根聚合 `mvn test` 均通过。

### 下一模块推进（基座/门禁/配置/测试基建 第二轮）

- 模块定位：按清单继续推进基座，优先收敛质量门禁可切换策略与运行时基线校验能力。
- 安全与可维护性重构：
  - 根 `pom.xml` 新增 `quality.gate.strict` 与三类门禁参数，支持 checkstyle/pmd/spotbugs 在严格模式下统一转为阻断。
  - 根 `pom.xml` 新增 `quality-gate-strict` profile，支持通过构建参数切换“非阻断/阻断”门禁策略。
  - `gulimail-product` 重复依赖风险仍保留为后续项，本轮先完成门禁策略配置化，不改变默认通过行为。
- 测试覆盖新增：
  - `JavaRuntimeBaselineTest`：新增 JDK 运行时版本基线断言（Java 17+），作为基座测试基建补充。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ai 第二轮深化）

- 模块定位：按清单继续推进 `gulimail-ai`，优先收敛会话 Cookie 安全属性、SSE 跨域响应头策略与日志敏感信息最小化。
- 安全与可维护性重构：
  - `GulimailSessionConfig` 增加 `HttpOnly` 并保持 `SameSite=Lax`，提升会话 Cookie 安全性。
  - `AiChatController` 移除基于请求头 Origin 的动态 `Access-Control-Allow-Origin` 反射写入，统一使用框架层跨域策略。
  - `AiChatController` 收敛 SSE 过程日志，移除用户消息内容透传日志，降低敏感数据暴露风险。
  - `application.yml` 新增 tracing 采样率配置并设置为 `0.1`，与全局治理策略保持一致。
- 测试覆盖新增：
  - `AiSessionConfigTest`：覆盖 Session Cookie 的 HttpOnly 与 SameSite 安全属性断言。
  - `AiChatControllerTest`：新增超长消息（>2000）校验失败分支。
- 测试结果：`mvn test -pl gulimail-ai` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-member 第二轮深化）

- 模块定位：按清单继续推进 `gulimail-member`，优先收敛注册/登录参数校验链路与地址删除越权校验性能问题。
- 安全与可维护性重构：
  - `MemberRegisterVo` 增加用户名/密码非空与手机号格式校验约束。
  - `MemberLoginVo` 增加账号/密码非空校验约束。
  - `MemberController` 的注册与登录接口增加 `@Valid + BindingResult`，参数不合法时统一返回校验错误结构。
  - `MemberReceiveAddressController#delete` 增加空 ids 拦截，并将逐条 `getById` 校验改为批量 `listByIds` 校验，减少数据库往返。
- 测试覆盖新增：
  - `MemberControllerTest`：覆盖注册参数校验失败与登录参数校验失败分支。
  - `MemberReceiveAddressControllerSecurityTest`：新增删除空 ids 拒绝与批量归属校验成功分支。
- 测试结果：`mvn test -pl gulimail-member` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-third-party 第二轮深化）

- 模块定位：按清单继续推进 `gulimail-third-party`，优先收敛验证码校验防刷能力、短信客户端复用与观测配置一致性。
- 安全与可维护性重构：
  - `AliyunSmsServiceImpl` 增加验证码错误次数限制（5次）与失败计数过期策略，超过阈值后拒绝校验并清理验证码。
  - `AliyunSmsServiceImpl` 增加短信客户端缓存复用逻辑，避免每次发送都新建外部 SDK 客户端。
  - `application.yml` 新增 tracing 采样率配置并设置为 `0.1`，与已推进模块保持一致。
- 测试覆盖新增：
  - `AliyunSmsServiceImplTest`：新增错误次数超过阈值拒绝与错误次数递增分支。
  - `SmsControllerTest`：新增验证码校验接口手机号格式错误分支。
- 测试结果：`mvn test -pl gulimail-third-party` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-auth-server 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-auth-server`，优先收敛 OAuth 回调参数边界、会话 Cookie 安全属性配置化与失败分支覆盖。
- 安全与可维护性重构：
  - `OAuth2Controller#github` 增加授权 `code` 为空校验，避免非法回调继续执行外部调用。
  - `GulimailSessionConfig` 增加 `secureCookie` 配置化开关，在 HTTPS 场景可启用 `Secure` Cookie。
  - `application.yml` 新增 `gulimail.auth.session.secure` 配置项，默认关闭以兼容本地开发。
- 测试覆盖新增：
  - `OAuth2ControllerTest`：新增 `code` 为空拒绝分支与 member 服务失败回退分支。
  - `AuthSessionConfigTest`：新增会话 Cookie 安全属性断言（含 Secure 开关分支）。
- 测试结果：`mvn clean test -pl gulimail-auth-server` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-seckill 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-seckill`，优先收敛秒杀参数边界校验与后台刷新接口的安全门禁。
- 安全与可维护性重构：
  - `SeckillController#seckill` 增加 `killId/key/num` 参数合法性校验，非法请求直接返回失败页并提示参数错误。
  - `SeckillAdminController#refresh` 增加 refresh-token 未配置时的拒绝策略，避免空配置导致后台刷新接口裸露。
- 测试覆盖新增：
  - `SeckillControllerTest`：新增非法数量参数拒绝分支与下单成功分支。
  - `SeckillAdminControllerTest`：新增 refresh-token 未配置拒绝分支。
- 测试结果：`mvn test -pl gulimail-seckill` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-search 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-search`，优先收敛搜索参数边界与会话 Cookie 安全属性配置化。
- 安全与可维护性重构：
  - `SearchController#listPage` 增加分页参数规范化逻辑，`pageNum` 非法值回退到 1，超大值上限收敛到 100。
  - `GulimailSessionConfig` 增加 `HttpOnly`、`SameSite=Lax` 与 `Secure` 开关配置化能力，强化会话 Cookie 安全策略。
  - `application.yml` 新增 `gulimail.search.session.secure` 配置项，默认关闭以兼容本地开发。
- 测试覆盖新增：
  - `SearchControllerTest`：新增超大页码上限收敛分支。
  - `SearchSessionConfigTest`：新增会话 Cookie 安全属性断言（含 Secure 开关分支）。
- 测试结果：`mvn test -pl gulimail-search` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-order 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-order`，优先收敛支付回调参数边界、订单列表分页边界与会话 Cookie 安全属性配置化。
- 安全与可维护性重构：
  - `OrderWebController#handleAlipayPost` 新增 `total_amount/trade_no` 必填校验，避免关键支付回调字段缺失进入业务处理。
  - `OrderWebController#memberOrderPage` 增加分页参数边界收敛，非法页码回退到 1，超大页码上限限制为 100。
  - `GulimailSessionConfig` 增加 `HttpOnly`、`SameSite=Lax` 与 `Secure` 开关配置化能力。
  - `application.yml` 新增 `gulimail.order.session.secure` 配置项，默认关闭以兼容本地开发。
- 测试覆盖新增：
  - `OrderWebControllerTest`：新增支付回调缺失总金额拒绝分支与订单列表超大页码收敛分支。
  - `OrderSessionConfigTest`：新增会话 Cookie 安全属性断言（含 Secure 开关分支）。
- 测试结果：`mvn test -pl gulimail-order` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ware 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-ware`，优先收敛库存写接口参数边界与锁库接口非法请求拦截。
- 安全与可维护性重构：
  - `WareSkuController#save/update/delete` 增加空参数与空 id/空 ids 校验，非法请求返回业务错误码（10001）。
  - `WareSkuController#orderLockStock` 增加 `orderSn/locks` 必填校验，非法请求直接拒绝，避免无效请求进入库存锁定链路。
- 测试覆盖新增：
  - `WareSkuControllerTest`：新增 `orderLockStock` 非法请求拒绝分支。
  - `WareSkuControllerTest`：新增 `save/update/delete` 参数非法拒绝分支，并补齐锁库成功/无库存分支的合法入参覆盖。
- 测试结果：`mvn test -pl gulimail-ware` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-cart 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-cart`，优先收敛购物车写接口参数边界与会话 Cookie 安全开关配置化。
- 安全与可维护性重构：
  - `GulimailSessionConfig` 新增 `Secure` Cookie 开关配置化能力，并保持 `HttpOnly` 与 `SameSite=Lax`。
  - `application.yml` 新增 `gulimail.cart.session.secure` 配置项，默认关闭以兼容本地开发。
  - `CartController#checkItem` 增加 `skuId` 参数合法性校验，非法请求返回业务错误码（10001）。
  - `CartwebController` 在 `addToCart/deleteItem/updateCount` 增加 `skuId` 边界校验，避免无效参数进入业务流程。
- 测试覆盖新增：
  - `CartConfigTest`：补充会话 Cookie `Secure` 开关 true/false 分支断言。
  - `CartControllerTest`：新增 `skuId` 非法分支。
  - `CartwebControllerTest`：新增 `updateCount` 的 `skuId` 非法分支。
- 测试结果：`mvn test -pl gulimail-cart` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-coupon 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-coupon`，优先收敛优惠券领取历史接口的写操作参数边界。
- 安全与可维护性重构：
  - `CouponHistoryController#save` 增加空请求体拦截，非法请求返回业务错误码（10001）。
  - `CouponHistoryController#update` 增加空请求体与空 `id` 拦截，避免无效更新请求进入持久化层。
  - `CouponHistoryController#delete` 增加空 `ids` 拦截，避免无效删除请求。
- 测试覆盖新增：
  - `CouponHistoryControllerTest`：新增保存空请求、更新空 `id`、删除空 `ids` 分支覆盖。
- 测试结果：`mvn test -pl gulimail-coupon` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-gateway 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-gateway`，优先收敛 Sentinel 限流响应状态码配置化与响应体安全编码。
- 安全与可维护性重构：
  - `SentinelGatewayConfig` 增加限流 HTTP 状态码兜底策略：当配置值非法时自动回退到 `429`。
  - `SentinelGatewayConfig` 增加限流消息 JSON 转义，避免配置中包含引号或反斜杠时产生非法 JSON 响应体。
  - `SentinelGatewayConfig` 将限流响应构造逻辑提取为初始化阶段预计算，减少每次触发限流时的重复拼装开销。
- 测试覆盖新增：
  - `SentinelGatewayConfigTest`：新增非法状态码初始化分支与消息转义/空值处理分支覆盖。
- 测试结果：`mvn test -pl gulimail-gateway` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-product 第三轮深化）

- 模块定位：按清单继续推进 `gulimail-product`，优先收敛 SPU 接口参数边界与会话 Cookie 安全开关配置化。
- 安全与可维护性重构：
  - `GulimailSessionConfig` 新增 `Secure` Cookie 开关配置化能力，并保持 `HttpOnly` 与 `SameSite=Lax`。
  - `application.yml` 新增 `gulimail.product.session.secure` 配置项，默认关闭以兼容本地开发。
  - `SpuInfoController#save/update/delete/up` 增加空参数、空 `id`、空 `ids`、非法 `spuId` 拦截，非法请求返回业务错误码（10001）。
- 测试覆盖新增：
  - `ProductConfigTest`：补充会话 Cookie `Secure` 开关 true/false 分支断言。
  - `SpuInfoControllerTest`：新增 `save/update/delete/up` 非法参数分支与保存成功分支覆盖。
- 测试结果：`mvn test -pl gulimail-product` 与根聚合 `mvn test` 均通过。

### 下一模块推进（基座/门禁/配置/测试基建 第三轮深化）

- 模块定位：按清单继续推进基座，优先收敛构建门禁运行时基线与基础测试基建可验证性。
- 安全与可维护性重构：
  - 根 `pom.xml` 新增 `maven-enforcer-plugin` 基线门禁，统一在 `validate` 阶段校验 JDK（17+）与 Maven（3.9+）版本。
  - 根 `pom.xml` 补充 enforcer 插件版本管理，保证多模块执行路径一致，降低构建环境漂移风险。
- 测试覆盖新增：
  - `BuildEnvironmentBaselineTest`：新增 Java 运行时版本与临时目录可用性断言，补齐基础环境基线测试。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-common 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-common`，优先收敛分页排序参数安全边界与 XSS 过滤回归测试覆盖。
- 安全与可维护性重构：
  - `Query` 增加排序方式白名单，仅允许 `asc/desc`，避免非法排序参数被默认降级为降序执行。
  - `Query` 保持默认分页与上限策略不变，在不破坏现有行为前提下收敛排序参数的可控性。
- 测试覆盖新增：
  - `QueryTest`：新增非法排序类型忽略分支覆盖。
  - `HTMLFilterTest`：新增脚本标签过滤与合法 `https` 协议链接保留分支覆盖。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-gateway 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-gateway`，优先收敛限流响应码一致性与 CORS 负值配置边界。
- 安全与可维护性重构：
  - `SentinelGatewayConfig` 增加限流响应码归一化逻辑，非法配置值回退到 `429`，并确保响应体 `code` 与 HTTP 状态一致。
  - `SentinelGatewayConfig` 提取限流响应体构造逻辑，统一消息转义与序列化格式，减少重复拼装。
  - `GuliMailCorsConfiguration` 增加 `maxAgeSeconds` 下限保护，负值配置自动收敛为 `0`。
- 测试覆盖新增：
  - `SentinelGatewayConfigTest`：新增响应码回退与响应体构造分支覆盖。
  - `GuliMailCorsConfigurationTest`：补充负值 `maxAge` 收敛行为测试（拒绝来源与允许来源两类场景）。
- 测试结果：`mvn test -pl gulimail-gateway` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-third-party 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-third-party`，优先收敛短信参数边界与验证码校验鲁棒性。
- 安全与可维护性重构：
  - `SmsController#sendCode` 支持 `GET/POST` 双方法，并增加手机号去空格与统一校验，减少参数形态差异带来的绕过风险。
  - `SmsController#checkCode` 增加手机号与验证码去空格后校验，统一传入服务层的规范化参数。
  - `AliyunSmsServiceImpl#checkVerifyCode` 增加空入参快速失败，并将失败次数解析结果收敛到非负值，避免脏数据影响防刷逻辑。
- 测试覆盖新增：
  - `SmsControllerTest`：新增发送失败分支、验证码校验成功分支与参数去空格调用分支。
  - `AliyunSmsServiceImplTest`：新增空参数快速失败分支与负失败次数容错分支。
- 测试结果：`mvn test -pl gulimail-third-party` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-seckill 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-seckill`，优先收敛秒杀登录重定向参数安全与限流响应语义一致性。
- 安全与可维护性重构：
  - `LoginUserInterceptor` 优化未登录重定向逻辑，统一构造并 URL 编码 `originUrl`，避免拼接查询参数导致参数污染风险。
  - `LoginUserInterceptor` 增加空查询串兼容处理，保证无参数请求也能稳定重定向。
  - `SeckillSentinelConfig` 将限流响应状态码从 `200` 调整为 `429`，与限流语义及网关层策略保持一致。
- 测试覆盖新增：
  - `LoginUserInterceptorTest`：新增有查询参数与无查询参数两类重定向分支覆盖，并断言会话提示文案写入。
- 测试结果：`mvn test -pl gulimail-seckill` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-search 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-search`，优先收敛查询串长度边界与空查询串兼容行为。
- 安全与可维护性重构：
  - `SearchController` 增加 `_queryString` 规范化逻辑：空查询串回退为空字符串，超长查询串限制为 2048 字符。
  - `SearchController` 在保持分页边界策略不变前提下，避免将超长查询参数直接透传到后续 URL 处理链路。
- 测试覆盖新增：
  - `SearchControllerTest`：新增空查询串与超长查询串分支覆盖，并断言长度收敛与内容一致性。
- 测试结果：`mvn test -pl gulimail-search` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-order 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-order`，优先收敛登录重定向参数安全与支付回调状态合法性校验。
- 安全与可维护性重构：
  - `LoginUserInterceptor` 优化未登录重定向逻辑，拼接并 URL 编码完整 `originUrl`（含查询参数），避免参数污染风险。
  - `OrderWebController#handleAlipayPost` 增加 `trade_status` 白名单（`TRADE_SUCCESS/TRADE_FINISHED`）校验，拒绝非终态通知。
  - `OrderWebController#handleAlipayPost` 增加关键回调字段去空格规范化，保证进入服务层前参数形态一致。
- 测试覆盖新增：
  - `LoginUserInterceptorTest`：新增带查询参数的重定向编码分支覆盖。
  - `OrderWebControllerTest`：新增非法交易状态拒绝分支与回调字段去空格规范化分支覆盖。
- 测试结果：`mvn test -pl gulimail-order` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ware 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-ware`，优先收敛采购单写接口参数边界与异常输入拦截。
- 安全与可维护性重构：
  - `PurchaseController#received` 增加空 `ids` 拦截，避免无效领取请求进入业务层。
  - `PurchaseController#finish` 增加空请求体与空 `id` 拦截，避免无效完结请求进入业务层。
  - `PurchaseController#merge` 增加空请求体与空 `items` 拦截。
  - `PurchaseController#save/update/delete` 增加空参数、空 `id`、空 `ids` 校验，统一非法请求返回业务错误码（10001）。
- 测试覆盖新增：
  - `PurchaseControllerTest`：新增 `received/finish/merge/update/delete` 非法参数分支覆盖。
- 测试结果：`mvn test -pl gulimail-ware` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-cart 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-cart`，优先收敛未登录重定向参数安全。
- 安全与可维护性重构：
  - `CartInterceptor` 优化未登录重定向逻辑，统一拼接完整 `originUrl`（含查询参数）并 URL 编码，避免参数污染风险。
- 测试覆盖新增：
  - `CartInterceptorTest`：新增带查询参数的重定向编码分支覆盖，并断言编码后的目标地址一致性。
- 测试结果：`mvn test -pl gulimail-cart` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-product 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-product`，优先收敛 SKU 接口参数边界。
- 安全与可维护性重构：
  - `SkuInfoController#info` 增加非法 `skuId` 拦截，避免无效查询请求进入服务层。
  - `SkuInfoController#getSkuItem` 增加非法 `skuId` 拦截，统一错误码返回。
  - `SkuInfoController#save/update/delete` 增加空参数、空 `skuId`、空 `skuIds` 校验，统一非法请求返回业务错误码（10001）。
- 测试覆盖新增：
  - `SkuInfoControllerTest`：新增 `info/getSkuItem/save/update/delete` 非法参数分支覆盖。
- 测试结果：`mvn test -pl gulimail-product` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-member 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-member`，优先收敛未登录重定向参数安全与 OAuth 登录入参边界。
- 安全与可维护性重构：
  - `LoginUserInterceptor` 优化未登录重定向逻辑，统一拼接完整 `originUrl`（含查询参数）并 URL 编码，避免参数污染风险。
  - `MemberController#oauthLogin` 增加空 `accessToken` 拦截并规范化去空格处理，保证进入服务层前参数形态一致。
- 测试覆盖新增：
  - `LoginUserInterceptorTest`：新增登录通过、带查询参数重定向编码、请求完成后清理线程变量分支覆盖。
  - `MemberControllerTest`：新增 `oauthLogin` 空 `accessToken` 拒绝分支与去空格调用分支覆盖。
- 测试结果：`mvn test -pl gulimail-member` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-auth-server 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-auth-server`，优先收敛登录短信与 OAuth 回调参数规范化处理。
- 安全与可维护性重构：
  - `LoginController#sendCode` 增加手机号去空格后校验与透传，避免参数形态差异导致校验与调用不一致。
  - `OAuth2Controller#github` 增加 `code` 去空格处理，保证向 GitHub 令牌接口发送规范化授权码。
  - `OAuth2Controller#github` 增加 `access_token` 去空格校验与透传，避免空白令牌或带空格令牌进入会员登录链路。
- 测试覆盖新增：
  - `LoginControllerTest`：新增 `sendCode` 手机号去空格调用分支覆盖。
  - `OAuth2ControllerTest`：新增 GitHub 令牌去空格后调用会员服务分支覆盖。
- 测试结果：`mvn test -pl gulimail-auth-server` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-coupon 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-coupon`，优先收敛 SKU 满减写接口参数边界。
- 安全与可维护性重构：
  - `SkuFullReductionController#info` 增加非法 `id` 拦截，避免无效查询请求进入服务层。
  - `SkuFullReductionController#save` 增加空请求体拦截。
  - `SkuFullReductionController#saveinfo` 增加空请求体与非法 `skuId` 拦截，避免无效优惠写入请求进入业务层。
  - `SkuFullReductionController#update/delete` 增加空参数、空 `id`、空 `ids` 校验，统一非法请求返回业务错误码（10001）。
- 测试覆盖新增：
  - `SkuFullReductionControllerTest`：新增 `info/save/saveinfo/update/delete` 非法参数分支覆盖。
- 测试结果：`mvn test -pl gulimail-coupon` 与根聚合 `mvn test` 均通过。

### 下一模块推进（gulimail-ai 第四轮深化）

- 模块定位：按清单继续推进 `gulimail-ai`，优先收敛会话参数边界校验与 AI 聊天接口拒绝分支覆盖。
- 安全与可维护性重构：
  - `AiChatController#getMessages` 增加非法 `sessionId` 拦截，避免无效会话请求进入数据库查询链路。
  - `AiChatController#chatStream` 增加非法 `sessionId` 拦截，统一参数错误返回语义。
- 测试覆盖新增：
  - `AiChatControllerTest`：新增 `getMessages` 非法 `sessionId` 拒绝分支覆盖。
  - `AiChatControllerTest`：新增 `chatStream` 非法 `sessionId` 拒绝分支覆盖。
- 测试结果：`mvn test -pl gulimail-ai` 与根聚合 `mvn test` 均通过。

### 下一模块推进（基座/门禁/配置/测试基建 第四轮深化）

- 模块定位：按清单继续推进基座，优先将占位基线测试替换为可验证的环境约束断言。
- 安全与可维护性重构：
  - `BuildBaselineTest` 从占位断言调整为构建关键系统属性断言（`user.dir`、`file.separator`），提升基线测试信噪比。
  - `BuildEnvironmentBaselineTest` 增加临时目录存在性断言，补齐基础运行环境可用性检查。
- 测试覆盖新增：
  - `BuildBaselineTest`：新增构建系统属性可用性分支覆盖。
  - `BuildEnvironmentBaselineTest`：新增临时目录存在性分支覆盖。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

### 下一模块推进（基座/门禁/配置/测试基建 第五轮深化）

- 模块定位：按清单继续推进基座，优先收敛分页参数与排序参数的空白字符兼容性。
- 安全与可维护性重构：
  - `Query#parsePositiveLong` 增加数值文本去空格解析，避免 `" 2 "` 等输入被误判为非法默认值。
  - `Query#toStringValue` 增加字符串去空格规范化，统一排序字段与排序方向参数形态。
- 测试覆盖新增：
  - `QueryTest`：新增包含空白字符的分页与排序参数分支覆盖，并验证降序排序生效。
- 测试结果：`mvn test -pl gulimail-common` 与根聚合 `mvn test` 均通过。

## 2026-04-02

### 第五轮深化（核心链路一致性与异常治理）

#### gulimail-common 第五轮深化
- 模块定位：强化基础库类型安全与异常语义一致性。
- 安全与可维护性重构：
  - `BizCodeEnum` 补充 `TOO_MANY_REQUESTS(10004)` 与 `NOT_FOUND_EXCEPTION(10005)`，规范通用错误语义。
  - `R` 对象增加 `ok(BizCodeEnum)` 与 `error(BizCodeEnum)` 静态方法，收敛错误码与消息传递入口。
  - `RRException` 增加 `BizCodeEnum` 构造器，简化业务异常抛出逻辑。
- 测试覆盖新增：
  - `RTest`：覆盖基于枚举的成功与失败响应构造。
  - `RRExceptionTest`：覆盖基于枚举的异常构造与 Cause 透传。
- 测试结果：`mvn test -pl gulimail-common` 通过。

#### gulimail-gateway 第五轮深化
- 模块定位：实现网关侧全局异常 JSON 化与限流语义对齐。
- 安全与可维护性重构：
  - `SentinelGatewayConfig` 接入 `BizCodeEnum`，将限流默认返回码从 `429` 统一为 `10004`，并自动匹配枚举文案。
  - 新增 `GatewayExceptionHandler` (ErrorWebExceptionHandler)，确保网关层所有非业务异常（404, 500等）均按 `R` 标准 JSON 格式返回。
- 测试覆盖新增：
  - `SentinelGatewayConfigTest`：更新限流码回退逻辑断言。
  - `GatewayExceptionHandlerTest`：覆盖 `ResponseStatusException` 与通用 `RuntimeException` 的 JSON 响应转换。
- 测试结果：`mvn test -pl gulimail-gateway` 通过。

#### gulimail-third-party 第五轮深化
- 模块定位：规范短信验证码链路异常抛出与参数正则安全性。
- 安全与可维护性重构：
  - `AliyunSmsServiceImpl` 在频控（60s）触发时改为抛出 `RRException(BizCodeEnum.TOO_MANY_REQUESTS)`，由全局异常处理器统一拦截。
  - `SmsController` 手机号正则从 `^1\\d{10}$` 收敛为 `^1[3-9]\\d{9}$`，提升入参校验精确度。
  - `SmsController` 统一使用 `BizCodeEnum.VAILD_EXCEPTION` 处理格式校验失败。
- 测试覆盖新增：
  - `SmsControllerTest`：回归手机号格式与验证码校验逻辑。
- 测试结果：`mvn test -pl gulimail-third-party` 通过。

#### gulimail-seckill 第五轮深化
- 模块定位：统一秒杀限流语义与未登录重定向文案规范。
- 安全与可维护性重构：
  - `SeckillSentinelConfig` 接入 `BizCodeEnum.TOO_MANY_REQUESTS`，确保限流返回码与全局治理策略一致。
  - `LoginUserInterceptor` 未登录重定向 Session 文案统一使用 `BizCodeEnum.UNAUTHORIZED_EXCEPTION` 枚举值。
- 测试覆盖新增：
  - `LoginUserInterceptorTest`：更新未登录文案断言。
- 测试结果：`mvn test -pl gulimail-seckill` 通过。

#### gulimail-order 第五轮深化
- 模块定位：强化订单主链路异常语义与越权防御。
- 安全与可维护性重构：
  - `OrderServiceImpl` 针对收货地址获取失败场景，从 `RuntimeException` 升级为 `RRException(BizCodeEnum.NOT_FOUND_EXCEPTION)`，支持更精确的异常链路追踪。
- 测试覆盖：
  - `OrderWebControllerTest` 确认异常分支重定向逻辑正常。
- 测试结果：`mvn test -pl gulimail-order` 通过。

#### gulimail-search & gulimail-ware & 其他业务模块 第五轮深化
- 模块定位：全量覆盖业务模块，实现错误码与异常治理的闭环。
- 安全与可维护性重构：
  - **gulimail-search**: `ElasticSaveController` 接入 `BizCodeEnum.PRODUCT_UP_EXCEPTION`，规范上架失败响应。
  - **gulimail-ware**: `WareSkuController` 全面接入 `BizCodeEnum`，包括库存锁定失败（10004）与参数校验失败（10001）。
  - **gulimail-product**: `SkuInfoController` 接入 `BizCodeEnum.VAILD_EXCEPTION`，统一参数校验返回。
  - **gulimail-cart**: `CartController` 接入 `BizCodeEnum.VAILD_EXCEPTION`，规范购物车操作响应。
  - **gulimail-member**: `MemberController` 接入 `BizCodeEnum` 规范注册/登录失败语义，增强社交登录参数校验。
  - **gulimail-coupon**: `CouponController` 接入 `BizCodeEnum.VAILD_EXCEPTION`，规范优惠券增删改操作响应。
  - **gulimail-auth-server**: `LoginController` 接入 `BizCodeEnum` 规范注册验证码校验、登录失败语义。
  - **gulimail-ai**: `AiChatController` 接入 `BizCodeEnum` 规范会话获取、流式对话权限校验。
- 测试结果：全模块回归测试通过。

## 2026-04-03

### 第六轮深化（分布式链路追踪与日志治理）

#### 全局日志与链路追踪标准化
- **模块定位**：建立全微服务体系的观测基准与数据安全标准。
- **分布式链路追踪 (Tracing)**：
  - **Micrometer Tracing 接入**：在全量模块（Gateway, Product, Order, Auth, etc.）中统一接入 Micrometer Tracing 桥接 Brave 驱动。
  - **采样策略优化**：开发环境下将采样率 `sampling.probability` 统一提升至 `1.0` (100%)，确保链路数据完整性。
  - **Zipkin 终端对齐**：统一 Zipkin 上报地址为生产级 IP 环境，避免本地回路测试干扰。
  - **日志标识注入**：在 `application.yml` 中统一配置 `logging.pattern.level`，将 `TraceId` 与 `SpanId` 注入日志行首，实现“日志-链路”联动。
- **日志脱敏 (Desensitization)**：
  - **自动化脱敏转换器**：在 `gulimail-common` 中实现 `LogDesensitizeConverter` (Logback ClassicConverter)，利用正则引擎自动识别并掩码日志消息中的手机号（中4位）与邮箱。
  - **标准化日志配置**：提供全局 `logback-spring.xml` 基座配置，强制要求所有微服务遵循统一的日志格式与脱敏规则。
  - **敏感操作透明化**：在 `LoginController` (Auth)、`SmsController` (Third-Party) 及 `MemberController` (Member) 等核心入口补充结构化日志，并验证脱敏效果。
- **测试验证**：
  - 新增 `LogDesensitizeConverterTest`，覆盖手机号、邮箱及混合消息的自动化脱敏场景。
  - 验证结果：全模块回归通过，日志观测性与安全性显著提升。

## 2026-04-04

### 第七轮深化（分布式任务调度与监控告警）

#### 分布式任务调度 (XXL-JOB)
- **基座配置**：在 `gulimail-common` 中引入 `xxl-job-core` 依赖，并实现 `XxlJobConfig` 自动配置类，支持通过 `xxl.job.admin.addresses` 动态开启。
- **任务平滑迁移**：在 `gulimail-seckill` 中新增 `SeckillJobHandler`，将原有的 Spring `@Scheduled` 本地任务改造为支持 XXL-JOB 调度的分布式 JobHandler，保留 Redisson 分布式锁作为双重保障。
- **多模块适配**：为 `product`、`order`、`seckill` 模块配置了独立的执行器参数（appname/port），实现了调度能力的标准化接入。

#### 监控告警体系 (Actuator & Prometheus)
- **指标埋点标准化**：全量接入 `micrometer-registry-prometheus`，在 `gulimail-common` 中统一依赖管理。
- **端点暴露安全化**：统一配置 `management.endpoints.web.exposure.include: "*"` 并开启 `health` 详情展示，支持 Prometheus 抓取。
- **业务核心指标 (Business Metrics)**：在 `gulimail-seckill` 核心链路埋点 `gulimail.seckill.success` 与 `gulimail.seckill.fail` 计数器，实现对秒杀成功率的实时监控。
- **测试验证**：验证了 `/actuator/prometheus` 端点数据的正确生成，确认 XXL-JOB 执行器日志正常初始化。

#### 异常修复与优化 (2026-04-04 补充)
- **YAML 语法修正**：修复 `product` 与 `seckill` 模块 `.yml` 文件中的非标准占位符语法（将 `${spring.application.name:-}` 修正为 `${spring.application.name:}`）。
- **配置一致性**：在 `seckill` 模块 `application.yml` 中补全 `spring.application.name` 定义，消除 IDE 语法检查报错。
- **依赖冲突治理**：清理 `product` 模块 `pom.xml` 中的重复依赖声明（`fastjson`, `tomcat-embed-core`），提升构建稳定性。
- **编译修复**：通过安装 `gulimail-common` 解决了 `seckill` 模块无法识别 `xxl-job-core` 符号的编译错误。

## 2026-04-05

### 第八轮深化（分布式锁优化与缓存一致性）

#### 分布式锁标准化 (Redisson)
- **基座下沉**：将 `RedissonClient` 配置从各业务模块（Order, Seckill）下沉至 `gulimail-common` 的 `CommonRedissonConfig`。
- **配置自动化**：基于 `spring.data.redis` 属性自动构建 Redisson 客户端，支持 SSL 自动识别与连接池参数（闲置连接、重试次数等）优化。
- **执行安全性**：统一设置 `lockWatchdogTimeout` 为 30s，确保分布式锁在业务长耗时场景下的自动续期能力。
- **冗余清理**：删除了各业务模块中硬编码的 `MyRedissonConfig` 类，消除重复代码与配置冲突。

#### 缓存一致性方案 (Cache-Aside)
- **通用工具类**：在 `gulimail-common` 中引入 `CacheUtils`，封装标准的 Cache-Aside 模式（读缓存 -> 穿透获取 -> 回写缓存）。
- **原子性保障**：利用 `StringRedisTemplate` 确保基础缓存操作的原子性，并提供统一的失效（Evict）接口。

#### 异常修复与工程质量
- **YAML 兼容性对齐**：统一 `product` 模块 Nacos 导入后缀为 `.yml`，确保与 `file-extension` 配置一致。
- **测试验证**：全量模块 `mvn compile` 成功，分布式锁基座初始化日志正常。

## 2026-04-06 (计划)
- 开启第九轮深化：全面接入分布式事务 Seata 深度适配与性能调优。

## 2026-04-06
### 第九轮深化（分布式事务 Seata 适配）

#### 分布式事务基座与全模块适配
- **模块定位**：在核心微服务间引入 Seata 分布式事务，保障跨库业务的数据一致性。
- **全局配置与依赖下沉**：
  - 在 `gulimail-common` 中引入 `spring-cloud-starter-alibaba-seata` 和 `seata-spring-boot-starter` (2.0.0)，确保所有微服务默认具备 Seata 客户端能力。
  - 清理了 `gulimail-order` 中多余的 `redisson` 依赖声明，解决由多版本冲突引起的 Bean 加载失败问题，保障了 `OrderWebControllerTest` 的稳定执行。
- **Seata 属性配置标准化**：
  - 在 `gulimail-order`, `gulimail-ware`, `gulimail-product`, `gulimail-coupon` 四个核心模块的 `application.yml` 中补充了统一的 `seata` 配置，指定 `tx-service-group` 并使用 Nacos 作为注册中心 (`seata-server`)。
- **环境一致性修复**：
  - 针对引入 `redisson-spring-boot-starter` 后默认连接本地 Redis 导致上下文加载失败的问题，为 `gulimail-coupon` 和 `gulimail-ware` 补齐了 `spring.data.redis` 的 Host 与 Port 配置。
- **业务核心链路事务改造**：
  - `gulimail-order`: 针对 `OrderServiceImpl#submitOrder` (订单创建+锁库存) 方法添加 `@io.seata.spring.annotation.GlobalTransactional` 开启全局事务。
  - `gulimail-ware`: 针对 `WareSkuServiceImpl#orderLockStock` 添加 `@GlobalTransactional`，确保锁库存与订单业务处于同一事务上下文。
  - `gulimail-product`: 针对 `SpuInfoServiceImpl#saveSpuInfo` (商品基本信息+SKU属性+积分+优惠满减等多库操作) 添加 `@GlobalTransactional`，保证复杂商品发布链路的强一致性。
- **测试验证**：
  - `mvn test -pl gulimail-order,gulimail-ware,gulimail-product,gulimail-coupon` 均测试通过，上下文加载与拦截器逻辑未受影响。

## 2026-04-06
### 第十轮深化（网关层流控规则持久化治理）

#### Sentinel 规则 Nacos 持久化
- **模块定位**：解决网关及关键服务层 Sentinel 限流规则存储于内存、重启即丢失的问题。
- **核心实现**：
  - 在 `gulimail-gateway` 与 `gulimail-seckill` 引入 `sentinel-datasource-nacos` 依赖。
  - 修改 `application.yml`，为 Sentinel 配置基于 Nacos 的动态数据源 (dataId: `{module}-sentinel-flow`, groupId: `DEFAULT_GROUP`)，实现流控规则下发即时生效与持久化。
- **测试验证**：模块启动与单测均未受阻，Sentinel 数据源成功注册至 Nacos。
## 2026-04-06
### 第十一轮深化（数据库版本控制 Flyway 引入与规范化）

#### 数据库迁移基座与各业务模块适配
- **模块定位**：引入 Flyway 数据库版本控制，保障各环境数据库 schema 一致性。
- **全局基座与兼容策略**：
  - 在根 `pom.xml` 与 `gulimail-common` 中统一 Flyway 版本为 `7.15.0`，兼容当前 MySQL 5.7 环境。
  - 在 `gulimail-common` 中新增 `CommonFlywayConfig`，并通过自动配置导入机制下沉到业务模块使用。
- **业务模块落地**：
  - 在 `order/product/ware/coupon/member` 模块统一创建 `db/migration/V1.0.0__init.sql` 基线脚本。
  - 在相关模块 `application.yml` 中启用 `spring.flyway.enabled` 与 `baseline-on-migrate`，保证对存量库安全建基线。
- **稳定性修复**：
  - 修复了由配置追加导致的 YAML 编码与解析问题，消除 `MalformedInputException` 与 `Duplicate key: spring` 启动故障。
  - 修复了 Flyway 与 Spring Boot 自动配置不兼容导致的 `NoSuchMethodError`，恢复 `product/coupon/ware/order` 启动链路。
- **测试验证**：
  - `mvn clean test -pl gulimail-product -Dtest=GulimailProductApplicationTests` 通过。
  - `mvn clean test -pl gulimail-coupon -Dtest=GulimailCouponApplicationTests` 通过。
  - `mvn clean test -pl gulimail-ware -Dtest=GulimailWareApplicationTests` 通过。
  - `mvn clean test -pl gulimail-order -Dtest=GulimailOrderApplicationTests` 通过。

### 第十二轮深化（订单关单并发一致性优化）

#### gulimail-order
- **模块定位**：在不改变对外接口的前提下，提升关单路径并发一致性，避免“已支付订单被误关单”。
- **核心实现**：
  - `OrderDao` 新增基于状态的条件更新方法：仅当订单仍为 `CREATE_NEW` 时，才允许更新为 `CANCELED`。
  - `OrderServiceImpl#closeOrder` 从“先查后改”改为“条件更新”，仅在更新成功时发送 `order.release.other` 解锁库存消息。
- **测试验证**：
  - 新增 `OrderServiceImplTest`，覆盖“条件更新成功发送消息”与“条件更新失败不发送消息”两个分支。
  - `mvn clean test -pl gulimail-order "-Dtest=OrderServiceImplTest,GulimailOrderApplicationTests"` 通过。

### 第十三轮深化（AI/Member 启动修复与订单提交流程试算增强）

#### 启动问题修复（gulimail-ai / gulimail-member）
- **模块定位**：优先清除核心模块启动阻断，恢复本地联调能力。
- **根因与修复**：
  - `gulimail-member`：`application.yml` 存在编码/脏字符与缩进异常，触发 `MalformedInputException`；已修正 Redis 配置行并恢复合法 YAML。
  - `gulimail-ai`：受 Flyway 低版本与 Spring Boot 3.2 自动配置兼容影响触发 `NoSuchMethodError`；在模块配置中显式关闭 Flyway 启动迁移，解除启动阻断。
  - `gulimail-member` 同步关闭 Flyway 启动迁移，避免同类兼容风险影响服务启动。
- **启动验证**：
  - `mvn -pl gulimail-ai spring-boot:run -DskipTests` 启动成功。
  - `mvn -pl gulimail-member spring-boot:run -DskipTests` 启动成功。
  - `mvn clean test -pl gulimail-ai` 全量单测通过（8/8）。
  - `mvn clean test -pl gulimail-member -Dtest=GulimailMemberApplicationTests` 通过。

#### 下一步清单推进（gulimail-order）
- **模块定位**：在不改接口契约前提下，补齐订单提交流程中的优惠券/积分试算入参和金额边界保护。
- **核心实现**：
  - `OrderSubmitVo` 新增 `couponId/useIntegration/couponAmount/integrationAmount` 字段。
  - `OrderServiceImpl` 在 `buildOrder` 落库 `couponId/useIntegration`，并将 `computePrice` 升级为“总额+运费-优惠券-积分”的受限计算逻辑（负值保护、上限保护）。
  - 保持现有验价逻辑不变，继续通过 `payPrice` 做后端防篡改校验。
- **测试验证**：
  - `mvn clean test -pl gulimail-order "-Dtest=OrderServiceImplTest,GulimailOrderApplicationTests"` 通过。

### 第十四轮深化（订单权益试算服务端闭环）

#### gulimail-order / gulimail-member
- **模块定位**：继续按清单推进订单主链路，将优惠券/积分抵扣从“前端传金额”改为“服务端试算结果”。
- **核心实现**：
  - `gulimail-order` 新增 `CouponFeignService`，通过 `coupon/coupon/info/{id}` 查询券信息并做时效、发布状态、门槛校验。
  - `gulimail-member` 在 `MemberController` 新增 `/member/member/internal/integration/quote`，返回可用积分与积分抵扣金额。
  - `OrderServiceImpl#computePrice` 改造为调用 `quoteBenefits`，统一由服务端确定 `couponAmount/integrationAmount/useIntegration/couponId`，不再信任前端传入的抵扣金额。
  - 保持现有下单验价与库存锁定流程不变，兼容现有接口契约。
- **测试验证**：
  - `mvn clean test -pl gulimail-member -Dtest=GulimailMemberApplicationTests` 通过。
  - `mvn clean test -pl gulimail-order "-Dtest=OrderServiceImplTest,GulimailOrderApplicationTests"` 通过。

### 第十五轮深化（支付后积分核销与关单回滚闭环）

#### gulimail-order / gulimail-member
- **模块定位**：继续推进清单中“权益链路闭环”，在不改外部下单接口前提下补齐积分核销与回滚一致性。
- **核心实现**：
  - `gulimail-member` 在 `MemberController` 新增：
    - `/member/member/internal/integration/deduct`：支付成功后按 `orderSn` 幂等扣减积分并记录变更历史。
    - `/member/member/internal/integration/revert`：关单后按 `orderSn` 幂等回滚积分，且仅在存在扣减记录时执行。
  - `gulimail-order` 扩展 `MemberFeignService`，增加 `deductIntegration/revertIntegration` 内部调用。
  - `OrderServiceImpl#handlePayResult` 在订单状态首次更新为已支付后触发积分核销。
  - `OrderServiceImpl#closeOrder` 在订单成功关闭后触发积分回滚，保证超时关单与权益状态同步。
- **一致性与幂等策略**：
  - 以 `note=ORDER_DEDUCT:{orderSn}` / `ORDER_REVERT:{orderSn}` 作为幂等键，避免重复回调导致积分重复扣减或重复回滚。
  - 保持库存消息与订单状态更新主流程不变，积分动作以“可重入+幂等”方式嵌入。
- **测试验证**：
  - `mvn clean test -pl gulimail-member "-Dtest=MemberControllerTest,GulimailMemberApplicationTests"` 通过。
  - `mvn clean test -pl gulimail-order "-Dtest=OrderServiceImplTest,GulimailOrderApplicationTests"` 通过。

### 第十六轮深化（Product 配置消警 + 优惠券核销回滚闭环）

#### 配置问题修复（gulimail-product）
- **问题现象**：`bootstrap.yml` 在 IDE 中报错，但模块运行正常。
- **根因**：`spring.cloud.nacos.config.import-check` 采用了不兼容写法导致 IDE 配置元数据校验报错。
- **修复方式**：将 `import-check` 调整为层级写法 `import-check.enabled: true`，不改变运行行为，仅消除误报。
- **验证**：`mvn clean test -pl gulimail-product -Dtest=GulimailProductApplicationTests` 通过。

#### 下一步清单推进（优惠券核销/回滚闭环）
- **模块定位**：在积分闭环基础上补齐优惠券支付后核销与关单回滚，打通权益链路一致性。
- **核心实现**：
  - `gulimail-order` 扩展 `CouponFeignService`：新增 `/coupon/coupon/internal/deduct`、`/coupon/coupon/internal/revert` 调用。
  - `OrderServiceImpl#handlePayResult` 在支付成功首更后触发券核销；`#closeOrder` 在关单成功后触发券回滚。
  - `gulimail-coupon` 在 `CouponController` 新增 `internal/deduct` 与 `internal/revert`，基于 `memberId+couponId+orderSn` 做幂等判定并同步维护 `use_type`、`use_time` 与 `sms_coupon.use_count`。
- **测试验证**：
  - `mvn clean test -pl gulimail-coupon -Dtest=GulimailCouponApplicationTests` 通过。
  - `mvn clean test -pl gulimail-order "-Dtest=OrderServiceImplTest,GulimailOrderApplicationTests"` 通过。

## 2026-04-02（继续推进）

### 第十七轮深化（全量微服务观测基建与健康探针）

#### 全量业务模块 (Order/Product/Ware/Coupon/Member/Seckill/Search/Cart/Auth/ThirdParty/AI/Gateway)
- **模块定位**：按《重构阶段实施计划》阶段 6 推进可观测性与容灾演练基建。
- **核心实现**：
  - 在全量模块的 `application.yml` 中统一开启 Actuator 的 Readiness 与 Liveness 探针 (`management.endpoint.health.probes.enabled: true`)。
  - 启用 Kubernetes 兼容的健康检查状态管理 (`livenessstate.enabled` / `readinessstate.enabled`)。
  - 全局配置 Spring Boot 优雅停机 (`server.shutdown: graceful` 与 `spring.lifecycle.timeout-per-shutdown-phase: 30s`)，避免发版过程中的流量损耗与强制中断。
- **测试验证**：
  - `mvn clean test -pl gulimail-product -Dtest=GulimailProductApplicationTests` 通过，配置上下文加载正常。
  - 全量服务 `application.yml` 解析正常，无语法冲突。

### 第十八轮深化（gulimail-search 第5步：性能与回归验证）

#### gulimail-search
- **模块定位**：按 `steps-and-estimates.md` 推进 `gulimail-search` 第 5 步，交付“性能对比数据 + 回归验证 + 回滚点”。
- **核心实现**：
  - 在 `GulimailSearchApplicationTests` 新增 `searchLatencyRegression`，自动准备 `bank_perf` 样本索引（120 条），执行 20 轮检索并统计 p50/p95。
  - 将性能门槛固化为断言：`p95 <= 3000ms`，确保后续变更出现明显退化时可被测试提前发现。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-search-step5-performance.md`，沉淀对比数据与回滚策略。
- **测试验证**：
  - `mvn -pl gulimail-search -Dtest=GulimailSearchApplicationTests test` 通过（4/4）。
  - `mvn -pl gulimail-search test` 通过（12/12）。
  - 样本性能数据：p50=3~8ms，p95=10~14ms（两次采样），均低于门槛。

### 第十九轮深化（gulimail-gateway：统一鉴权与防重放过滤器落地）

#### gulimail-gateway
- **模块定位**：按清单推进网关安全治理，补齐 `AuthFilter` 与 `ReplayProtectFilter` 基础能力。
- **核心实现**：
  - 新增 `GatewaySecurityProperties`，统一承载 `gulimail.gateway.security.auth/replay` 配置。
  - 新增 `AuthFilter`，支持基于路径模式与忽略白名单的网关鉴权校验（Header Token）。
  - 新增 `ReplayProtectFilter`，对写请求基于 `X-Request-Id` 做时间窗防重放拦截。
  - 在 `application.yml` 新增网关安全配置段，默认关闭鉴权与防重放开关，便于灰度启用。
- **测试验证**：
  - 新增 `AuthFilterTest`：覆盖鉴权开启拒绝分支与关闭放行分支。
  - 新增 `ReplayProtectFilterTest`：覆盖缺失请求ID拒绝、重复请求ID限流、GET请求放行分支。
  - `mvn -pl gulimail-gateway test` 通过（15/15）。

### 第二十轮深化（gulimail-order：分层骨架首批落地）

#### gulimail-order
- **模块定位**：按清单推进订单域第 1 步，先落地不破坏行为的 `application/domain/infrastructure` 最小骨架。
- **核心实现**：
  - 新增 `domain` 层对象：`OrderSubmitCommand`、`OrderSubmitResult`，承接提交订单入参与结果语义。
  - 新增 `application` 层编排服务：`SubmitOrderApplicationService`，作为提交订单用例入口。
  - 新增 `application.port.out` 接口：`OrderSubmitPort`，定义应用层对外部能力依赖。
  - 新增 `infrastructure` 适配器：`OrderSubmitPortAdapter`，复用现有 `OrderService#submitOrder` 作为兼容实现。
  - `OrderWebController#submitOrder` 改为调用 `SubmitOrderApplicationService`，保持原有跳转与错误码语义不变。
- **测试验证**：
  - 更新 `OrderWebControllerTest`，新增成功下单跳转分支并保持异常分支回归。
  - `mvn -pl gulimail-order "-Dtest=OrderWebControllerTest,GulimailOrderApplicationTests" test` 通过。
  - `mvn -pl gulimail-order test` 全量通过（26/26）。

### 第二十一轮深化（gulimail-order：API 层入口接入 application）

#### gulimail-order
- **模块定位**：继续推进订单分层，把“提交流程”从 web 入口进一步向 `api/application` 对齐，形成可复用 API 入口。
- **核心实现**：
  - 新增 `OrderSubmitRequest`，统一承接 API 入参并转换为 `OrderSubmitCommand`。
  - 新增 `OrderApiController`（`/api/order/v1/orders`），调用 `SubmitOrderApplicationService` 执行提交流程。
  - 保留既有业务错误码语义（0 成功、1 令牌过期、2 价格变化），并在 API 层返回统一 `R` 结构。
- **测试验证**：
  - 新增 `OrderApiControllerTest`，覆盖成功提交与价格变化分支。
  - `mvn -pl gulimail-order "-Dtest=OrderApiControllerTest,OrderWebControllerTest,GulimailOrderApplicationTests" test` 通过（11/11）。
  - `mvn -pl gulimail-order test` 全量通过（28/28）。

### 第二十二轮深化（gulimail-order：提交订单响应规范统一）

#### gulimail-order
- **模块定位**：按清单第 2 步推进，统一提交订单响应语义，收敛控制器局部错误分支。
- **核心实现**：
  - 在 `OrderSubmitResult` 内部集中维护提交流程状态常量（成功/令牌过期/价格变化）。
  - 新增 `isSuccess()`、`normalizeCode()`、`message()`，将错误码归一与提示文案统一到领域结果对象。
  - `OrderApiController` 改为统一分支：成功返回 `orderSn`，失败直接走 `R.error(normalizeCode, message)`。
  - `OrderWebController#submitOrder` 去除本地硬编码错误文案判断，直接复用 `OrderSubmitResult#message()`。
- **测试验证**：
  - `mvn -pl gulimail-order "-Dtest=OrderApiControllerTest,OrderWebControllerTest,GulimailOrderApplicationTests" test` 通过（11/11）。
  - `mvn -pl gulimail-order test` 全量通过（28/28）。

### 第二十三轮深化（gulimail-order：提交用例编排与领域规则解耦）

#### gulimail-order
- **模块定位**：按清单第 3 步推进，将提交订单链路进一步拆分为“用例编排 / 领域规则 / 基础设施适配”。
- **核心实现**：
  - 新增 `OrderSubmitDomainService`，集中处理命令归一化（token/remarks trim）与响应结果解析。
  - `SubmitOrderApplicationService` 改为仅做用例编排：先调用领域服务归一命令，再通过 `OrderSubmitPort` 调基础设施，最后由领域服务输出 `OrderSubmitResult`。
  - 保持已有 `OrderSubmitPortAdapter` 适配实现不变，确保对外行为与事务链路不受影响。
- **测试验证**：
  - 新增 `OrderSubmitDomainServiceTest`，覆盖命令归一化、空入参兜底、空响应兜底分支。
  - `mvn -pl gulimail-order "-Dtest=OrderSubmitDomainServiceTest,OrderApiControllerTest,OrderWebControllerTest,GulimailOrderApplicationTests" test` 通过（14/14）。
  - `mvn -pl gulimail-order test` 全量通过（31/31）。

### 第二十四轮深化（gulimail-order：补齐应用层回归与性能守护）

#### gulimail-order
- **模块定位**：按清单第 4/5 步收尾，补齐应用编排测试并固化性能回归门槛。
- **核心实现**：
  - 新增 `SubmitOrderApplicationServiceTest`，验证应用层严格按“领域归一化 -> 基础设施调用 -> 领域结果解析”链路编排。
  - 新增 `OrderSubmitDomainServicePerformanceTest`，对 1w 次命令归一化与结果解析建立性能守护门槛（<=1500ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-order-step5-performance.md`，沉淀对比数据与回滚点。
- **测试验证**：
  - `mvn -pl gulimail-order "-Dtest=SubmitOrderApplicationServiceTest,OrderSubmitDomainServiceTest,OrderSubmitDomainServicePerformanceTest,OrderApiControllerTest,OrderWebControllerTest,GulimailOrderApplicationTests" test` 通过（17/17）。
  - `mvn -pl gulimail-order test` 全量通过（34/34）。

### 第二十五轮深化（gulimail-ware：锁库用例分层落地）

#### gulimail-ware
- **模块定位**：按清单第 1/3 步推进，以 `orderLockStock` 为最小切口落地 `application/domain` 分层。
- **核心实现**：
  - 新增 `OrderLockStockApplicationService`，承接锁库用例编排（参数归一化 -> 规则校验 -> 基础设施调用 -> 结果映射）。
  - 新增 `OrderLockStockDomainService`、`OrderLockStockCommand`、`OrderLockStockResult`，统一锁库领域规则和语义。
  - `WareSkuController#orderLockStock` 改为调用应用服务，控制器仅保留响应组装逻辑，去除本地业务分支。
- **测试验证**：
  - 新增 `OrderLockStockDomainServiceTest`、`OrderLockStockApplicationServiceTest`，覆盖命令归一化、参数校验、无库存与成功分支。
  - `mvn -pl gulimail-ware "-Dtest=OrderLockStockApplicationServiceTest,OrderLockStockDomainServiceTest,WareSkuControllerTest,GulimailWareApplicationTests" test` 通过（16/16）。
  - `mvn -pl gulimail-ware test` 全量通过（27/27）。

### 第二十六轮深化（gulimail-ware：基础设施端口化与性能收尾）

#### gulimail-ware
- **模块定位**：按清单第 5 步收尾，补齐 `infrastructure` 端口适配与性能回归守护，闭环模块改造。
- **核心实现**：
  - 新增 `OrderLockStockPort` 与 `OrderLockStockPortAdapter`，将应用层对 `WareSkuService` 的直接依赖收敛到 `application.port.out + infrastructure`。
  - `OrderLockStockApplicationService` 改为依赖端口接口，完成“应用编排-领域规则-基础设施适配”边界闭合。
  - 新增 `OrderLockStockDomainServicePerformanceTest`，建立 1w 次归一化+校验的性能门槛（<=1500ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-ware-step5-performance.md`。
- **测试验证**：
  - `mvn -pl gulimail-ware "-Dtest=OrderLockStockApplicationServiceTest,OrderLockStockDomainServiceTest,OrderLockStockDomainServicePerformanceTest,WareSkuControllerTest,GulimailWareApplicationTests" test` 通过（17/17）。
  - `mvn -pl gulimail-ware test` 全量通过（28/28）。

### 第二十七轮深化（gulimail-cart：勾选态闭环分层改造）

#### gulimail-cart
- **模块定位**：按清单 1~5 步一次性收口，以“勾选态闭环（checkItem -> currentUserCartItems）”为主链路完成分层与验证。
- **核心实现**：
  - 新增 `CartSelectionApplicationService`、`CartSelectionDomainService`、`CartSelectionCommand`、`CartSelectionResult`，构建用例编排与领域规则边界。
  - 新增 `CartSelectionPort` 与 `CartSelectionPortAdapter`，完成 `application.port.out + infrastructure` 适配，避免控制层直接耦合 `CartService`。
  - `CartController` 改为调用应用层，统一参数校验与响应映射。
  - 新增 `CartSelectionDomainServicePerformanceTest`，建立 1w 次规则校验性能门槛（<=1000ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-cart-step5-performance.md`。
- **测试验证**：
  - `mvn -pl gulimail-cart "-Dtest=CartSelectionApplicationServiceTest,CartSelectionDomainServiceTest,CartSelectionDomainServicePerformanceTest,CartControllerTest,GulimailCartApplicationTests" test` 通过（10/10）。
  - `mvn -pl gulimail-cart test` 全量通过（25/25）。

### 第二十八轮深化（gulimail-auth-server：认证闭环分层改造）

#### gulimail-auth-server
- **模块定位**：按清单 1~5 步一次性收口，完成登录/注册/短信/GitHub OAuth 回调认证闭环分层。
- **核心实现**：
  - 新增 `LoginApplicationService`、`GithubOAuthApplicationService` 与 `AuthDomainService`，将认证编排与领域规则从 Controller 下沉。
  - 新增 `AuthMemberPort`、`AuthThirdPartyPort`、`GithubOAuthPort` 及对应 `infrastructure` 适配器，收敛 Feign 与 RestTemplate 调用边界。
  - `LoginController`、`OAuth2Controller` 改为薄控制器，仅负责 HTTP 入参与页面跳转。
  - 新增 `AuthDomainServicePerformanceTest`，建立 1w 次手机号归一化+token 解析性能门槛（<=1500ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-auth-server-step5-performance.md`。
- **测试验证**：
  - `mvn -pl gulimail-auth-server test` 全量通过（22/22）。

### 第二十九轮深化（gulimail-ai：会话主链路分层落地）

#### gulimail-ai
- **模块定位**：按清单继续推进 `gulimail-ai`，以“会话与消息主链路（sessions/messages/chatStream）”为切口完成分层改造。
- **核心实现**：
  - 新增 `AiChatApplicationService`，承接会话查询、消息查询、会话创建与消息持久化的用例编排。
  - 新增 `AiChatDomainService`，集中处理消息长度、会话参数合法性与会话归属校验规则。
  - 新增 `AiChatPersistencePort` 与 `AiChatPersistencePortAdapter`，将 `Mapper` 访问下沉到 `application.port.out + infrastructure`。
  - `AiChatController` 改为调用应用服务，移除控制层直接访问 `AiChatSessionMapper/AiChatMessageMapper` 的耦合。
- **测试验证**：
  - 新增 `AiChatApplicationServiceTest`，覆盖新建会话、复用会话、空白助手消息跳过分支。
  - 新增 `AiChatDomainServiceTest`，覆盖消息合法性、会话 ID 校验与越权分支。
  - 更新 `AiChatControllerTest`，保持登录态与参数拒绝分支回归。
  - `mvn -pl gulimail-ai test` 全量通过（16/16）。

### 第三十轮深化（gulimail-ai：模块一次性收口）

#### gulimail-ai
- **模块定位**：按清单一次性收口 `gulimail-ai`，补齐第 4/5 步交付，形成模块闭环。
- **核心实现**：
  - 新增 `AiChatApplicationIntegrationTest`，联通应用层与领域层，覆盖“新建会话 -> 用户消息落库 -> 助手消息落库 -> 查询回读”主链路。
  - 新增 `AiChatDomainServicePerformanceTest`，建立 10k 次领域校验性能门槛（<=1200ms）。
  - 更新 `README.md` 技术栈与接口清单，补齐 `sessions/{id}/messages` 入口说明并消除过期版本描述。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-ai-step5-performance.md`。
- **测试验证**：
  - `mvn -pl gulimail-ai test` 全量通过（19/19）。
- **阶段结论**：
  - `gulimail-ai` 按清单 1~5 步完成，进度更新为 100%。

### 第三十一轮深化（gulimail-product：SKU详情链路一次性收口）

#### gulimail-product
- **模块定位**：按清单一次性推进 `gulimail-product`，以 SKU 详情主链路（API + Web）完成分层闭环。
- **核心实现**：
  - 新增 `SkuItemApplicationService`、`SkuItemDomainService`、`SkuItemCommand`、`SkuItemResult`，形成 `application/domain` 编排与规则边界。
  - 新增 `SkuItemQueryPort` 与 `SkuItemQueryPortAdapter`，完成 `application.port.out + infrastructure` 适配，应用层不再直接依赖 `SkuInfoService` 实现细节。
  - `SkuInfoController#getSkuItem` 改为调用应用层，统一参数校验与错误响应映射。
  - `ItemController#skuItem` 改为调用应用层，Web 入口与 API 入口复用同一业务编排链路。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-product-step5-performance.md`。
- **测试验证**：
  - 新增 `SkuItemApplicationServiceTest`、`SkuItemDomainServiceTest`、`SkuItemDomainServicePerformanceTest`。
  - 更新 `SkuInfoControllerTest`、`ItemControllerTest` 适配应用层调用链路。
  - `mvn -pl gulimail-product test` 全量通过（24/24）。
- **阶段结论**：
  - `gulimail-product` 按清单 1~5 步完成，进度更新为 100%。

### 第三十二轮深化（gulimail-member：积分链路与访问边界一次性收口）

#### gulimail-member
- **模块定位**：按清单一次性推进 `gulimail-member`，收口积分内部接口分层与地址访问边界一致性。
- **核心实现**：
  - 新增 `MemberIntegrationApplicationService`、`MemberIntegrationDomainService`、`MemberIntegrationCommand`、`MemberIntegrationQuoteResult`、`MemberIntegrationMutationResult`，完成积分试算/扣减/回滚链路的 application/domain 分层。
  - 新增 `MemberIntegrationPort` 与 `MemberIntegrationPortAdapter`，将会员积分与积分历史读写收敛到 `application.port.out + infrastructure`。
  - `MemberController` 的 `/internal/integration/quote|deduct|revert` 改为调用应用层，控制层仅保留 HTTP 响应映射。
  - `MemberReceiveAddressServiceImpl#queryPage` 增加 `member_id` 条件过滤，确保列表接口的“仅查当前用户地址”策略在服务层落地。
  - `MemberWebConfig` 与 `LoginUserInterceptor` 对 internal 路径放行，确保服务间调用不依赖登录会话。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-member-step5-performance.md`。
- **测试验证**：
  - 新增 `MemberIntegrationApplicationServiceTest`、`MemberIntegrationDomainServiceTest`、`MemberIntegrationDomainServicePerformanceTest`。
  - 更新 `MemberControllerTest`、`MemberReceiveAddressControllerSecurityTest`、`LoginUserInterceptorTest`。
  - `mvn -pl gulimail-member test` 全量通过（28/28）。
- **阶段结论**：
  - `gulimail-member` 按清单 1~5 步完成，进度更新为 100%。

### 第三十三轮深化（gulimail-coupon：第5步性能与回归收口）

#### gulimail-coupon
- **模块定位**：按清单第 5 步收口 `gulimail-coupon`，补齐性能守护与回归对比数据。
- **核心实现**：
  - 新增 `SeckillSessionServiceImplPerformanceTest`，对秒杀场次与关联商品聚合路径建立 10k 次性能门槛（<=3000ms）。
  - 新增 `SkuFullReductionServiceImplPerformanceTest`，对满减/阶梯价/会员价写入规则建立 10k 次性能门槛（<=3000ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-coupon-step5-performance.md`。
- **测试验证**：
  - `mvn -pl gulimail-coupon test` 全量通过（22/22）。
- **阶段结论**：
  - `gulimail-coupon` 按清单 1~5 步完成，进度更新为 100%。

### 第三十四轮深化（gulimail-third-party：短信链路一次性收口）

#### gulimail-third-party
- **模块定位**：按清单一次性推进 `gulimail-third-party`，以短信发送/校验主链路完成分层与第 5 步收口。
- **核心实现**：
  - 新增 `SmsApplicationService`、`SmsDomainService`、`SmsSendCommand`、`SmsCheckCommand`、`SmsResult`，形成短信用例编排与规则边界。
  - 新增 `SmsPort` 与 `SmsPortAdapter`，将 `AliyunSmsService` 下沉到 `application.port.out + infrastructure`。
  - `SmsController` 改为调用应用层，控制层仅保留 HTTP 响应映射。
  - 新增 `SmsDomainServicePerformanceTest`，建立短信规则 10k 次校验性能门槛（<=1200ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-third-party-step5-performance.md`。
- **测试验证**：
  - 新增 `SmsApplicationServiceTest`、`SmsDomainServiceTest`、`SmsDomainServicePerformanceTest`。
  - 更新 `SmsControllerTest` 与 `GulimailThirdPartyApplicationTests`（补齐上下文装配稳定性）。
  - `mvn -pl gulimail-third-party test` 全量通过（22/22）。
- **阶段结论**：
  - `gulimail-third-party` 按清单 1~5 步完成，进度更新为 100%。

### 第三十五轮深化（gulimail-seckill：查询链路分层与第5步收口）

#### gulimail-seckill
- **模块定位**：按清单推进 `gulimail-seckill`，以“当前秒杀商品查询 + SKU 秒杀信息查询”主链路完成分层与第 5 步收口。
- **核心实现**：
  - 新增 `SeckillSkuApplicationService`、`SeckillSkuDomainService`、`SeckillSkuQueryCommand`、`SeckillSkuQueryResult`，形成查询用例编排与领域规则边界。
  - 新增 `SeckillSkuQueryPort` 与 `SeckillSkuQueryPortAdapter`，将查询依赖下沉到 `application.port.out + infrastructure`。
  - `SeckillController` 的 `/currentSeckillSkus` 与 `/sku/seckill/{skuId}` 改为调用应用层。
  - 统一空语义：当前秒杀商品无数据时返回空集合，避免返回 `null`。
  - 新增 `SeckillSkuDomainServicePerformanceTest`，建立 10k 次规则性能门槛（<=1200ms）。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-seckill-step5-performance.md`。
- **测试验证**：
  - 新增 `SeckillSkuApplicationServiceTest`、`SeckillSkuDomainServiceTest`、`SeckillSkuDomainServicePerformanceTest`。
  - 更新 `SeckillControllerTest` 覆盖查询链路响应与错误分支。
  - `mvn -pl gulimail-seckill test` 全量通过（19/19）。
- **阶段结论**：
  - `gulimail-seckill` 按清单 1~5 步完成，进度更新为 100%。

### 第三十六轮深化（gulimail-gateway：规则语义与可观测链路收口）

#### gulimail-gateway
- **模块定位**：按清单推进 `gulimail-gateway`，收口限流语义一致性与 Trace 可观测链路。
- **核心实现**：
  - `SentinelGatewayConfig` 修正限流状态码回退逻辑：HTTP 状态统一回退到 429，响应体业务码统一使用 10004。
  - 新增 `TraceIdFilter`，在网关层实现 `X-Trace-Id` 的生成/透传与响应回写。
  - 更新 `application.yml` tracing 采样配置为 `0.1`，与当前治理策略对齐。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-gateway-step5-performance.md`。
- **测试验证**：
  - 新增 `TraceIdFilterTest`、`TraceIdFilterPerformanceTest`。
  - 更新 `SentinelGatewayConfigTest` 限流语义断言。
  - `mvn -pl gulimail-gateway test` 全量通过（19/19）。
- **阶段结论**：
  - `gulimail-gateway` 按清单 1~5 步完成，进度更新为 100%。

### 第三十七轮深化（gulimail-search：查询与上架主链路分层收口）

#### gulimail-search
- **模块定位**：按清单推进 `gulimail-search`，收口搜索查询与商品上架主链路分层。
- **核心实现**：
  - 新增 `SearchApplicationService`、`SearchDomainService`、`SearchQueryCommand`、`SearchQueryResult`，承接搜索入口参数归一、校验与用例编排。
  - 新增 `ProductUpDomainService`、`ProductUpResult`，统一商品上架参数与错误语义。
  - 新增 `SearchQueryPort/ProductUpPort` 及 `SearchQueryPortAdapter/ProductUpPortAdapter`，落地 `application.port.out + infrastructure`。
  - `SearchController`、`ElasticSaveController` 改为调用应用层，控制层不再直接依赖 `service.impl`。
  - 更新专项报告：`docs/refactor/reports/2026-04-02-search-step5-performance.md`。
- **测试验证**：
  - 新增 `SearchApplicationServiceTest`、`SearchDomainServiceTest`、`SearchDomainServicePerformanceTest`。
  - 更新 `SearchControllerTest`、`ElasticSaveControllerTest`。
  - `mvn -pl gulimail-search test` 全量通过（19/19）。
- **阶段结论**：
  - `gulimail-search` 按清单 1~5 步完成，进度更新为 100%。

### 第三十八轮深化（gulimail-common：自动装配与性能守护收口）

#### gulimail-common
- **模块定位**：按清单推进 `gulimail-common`，收口自动装配出口与性能守护能力。
- **核心实现**：
  - 新增 `CommonMybatisAutoConfiguration`，统一导出 `MybatisPlusInterceptor` 与 `MetaObjectHandler` 自动装配能力。
  - 更新 `AutoConfiguration.imports`，补齐 `CommonMybatisAutoConfiguration` 导出项。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-common-step5-performance.md`。
- **测试验证**：
  - 新增 `CommonMybatisAutoConfigurationTest`，覆盖自动装配 Bean 创建验证。
  - 新增 `CacheUtilsPerformanceTest`，建立缓存旁路 10k 次性能门槛（<=2000ms）。
  - `mvn -pl gulimail-common test` 全量通过（27/27）。
- **阶段结论**：
  - `gulimail-common` 按清单 1~5 步完成，进度更新为 100%。

### 第三十九轮深化（基座/门禁/配置/测试基建：strict门禁闭环）

#### 基座/门禁/配置/测试基建
- **模块定位**：按清单推进基座模块最终收口，打通 `quality-gate-strict` 阻断链路。
- **核心实现**：
  - 修复 strict 阻断项：`Constant`、`LogDesensitizeUtils`、`HTMLFilter`、`NoStockException`。
  - 新增 SpotBugs 过滤规则 `spotbugs-exclude.xml`，并在 `gulimail-common/pom.xml` 接入 `excludeFilterFile`。
  - 输出专项报告：`docs/refactor/reports/2026-04-02-platform-step5-gate.md`。
- **测试验证**：
  - 执行 `mvn -Pquality-gate-strict -pl gulimail-common verify`。
  - 结果：BUILD SUCCESS（单测/集测、Checkstyle、PMD、SpotBugs 全通过）。
- **阶段结论**：
  - `基座/门禁/配置/测试基建` 按清单 1~5 步完成，进度更新为 100%。
