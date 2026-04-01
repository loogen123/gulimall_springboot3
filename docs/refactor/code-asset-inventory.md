# gulimail 代码资产清单（自动盘点基线）

本文档面向“重构前全景阅读/资产盘点”评审，用于给出当前仓库中 Spring Boot 启动入口、核心 Spring 配置类、MyBatis-Plus Mapper、Feign Client、定时任务、消息监听器的基线清单（以源码扫描结果为准）。

## 1. Spring Boot 启动类清单（@SpringBootApplication）

| 模块 | 启动类 | 关键启用/排除 |
| --- | --- | --- |
| gulimail-ai | [GulimailAiApplication](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/GulimailAiApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients(basePackages="com.lg.gulimail.ai.feign")`、`@MapperScan("com.lg.gulimail.ai.mapper")`、`@EnableRedisHttpSession` |
| gulimail-auth-server | [GulimailAuthServerApplication](file:///d:/GitProgram/gulimail/gulimail-auth-server/src/main/java/com/lg/gulimail/authserver/GulimailAuthServerApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients`、排除 `DataSourceAutoConfiguration` |
| gulimail-cart | [GulimailCartApplication](file:///d:/GitProgram/gulimail/gulimail-cart/src/main/java/com/lg/gulimail/cart/GulimailCartApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients`、排除 `DataSourceAutoConfiguration` |
| gulimail-coupon | [GulimailCouponApplication](file:///d:/GitProgram/gulimail/gulimail-coupon/src/main/java/com/lg/gulimail/coupon/GulimailCouponApplication.java) | `@EnableDiscoveryClient`、`@MapperScan("com.lg.gulimail.coupon.dao")` |
| gulimail-gateway | [GulimailGatewayApplication](file:///d:/GitProgram/gulimail/gulimail-gateway/src/main/java/com/lg/gulimail/gateway/GulimailGatewayApplication.java) | `@EnableDiscoveryClient`、`scanBasePackages={"com.lg.gulimail.gateway"}`、排除 `DataSourceAutoConfiguration` |
| gulimail-member | [GulimailMemberApplication](file:///d:/GitProgram/gulimail/gulimail-member/src/main/java/com/lg/gulimail/member/GulimailMemberApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients(basePackages="com.lg.gulimail.member.feign")` |
| gulimail-order | [GulimailOrderApplication](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/GulimailOrderApplication.java) | `@EnableRabbit`、`@EnableFeignClients`、`@ComponentScan({"com.lg.gulimail.order","com.lg.common.config"})` |
| gulimail-product | [GulimailProductApplication](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/GulimailProductApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients(basePackages="com.lg.gulimail.product.feign")`、`@MapperScan("com.lg.gulimail.product.dao")`、`@EnableCaching` |
| gulimail-search | [GulimailSearchApplication](file:///d:/GitProgram/gulimail/gulimail-search/src/main/java/com/lg/gulimail/search/GulimailSearchApplication.java) | `@EnableDiscoveryClient` |
| gulimail-seckill | [GulimailSeckillApplication](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/GulimailSeckillApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients`、`@EnableAsync`、`@EnableScheduling`、排除 `DataSourceAutoConfiguration` |
| gulimail-third-party | [GulimailThirdPartyApplication](file:///d:/GitProgram/gulimail/gulimail-third-party/src/main/java/com/lg/gulimail/thirdparty/GulimailThirdPartyApplication.java) | `@EnableDiscoveryClient` |
| gulimail-ware | [GulimailWareApplication](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/GulimailWareApplication.java) | `@EnableDiscoveryClient`、`@EnableFeignClients`、`@MapperScan("com.lg.gulimail.ware.dao")`、`@EnableTransactionManagement` |
| renren-fast | [RenrenApplication](file:///d:/GitProgram/gulimail/renren-fast/src/main/java/io/renren/RenrenApplication.java) | `@SpringBootApplication`（独立系统） |
| renren-generator | [RenrenApplication](file:///d:/GitProgram/gulimail/renren-generator/src/main/java/io/renren/RenrenApplication.java) | `@SpringBootApplication(exclude={MongoAutoConfiguration,MongoDataAutoConfiguration})`（独立系统） |

## 2. Spring 配置类清单（@Configuration）

自动扫描到 61 个 `@Configuration` 类（含 gulimail 与 renren 系列）。为便于评审与重构分层改造，建议后续在阶段 0 将其按“配置类型”再细分为：
`WebMvc`、`Security`、`Feign`、`Redis/Cache`、`MyBatis`、`MQ`、`ThreadPool`、`Session`、`Sentinel/Gateway` 等。

本次基线仅给出关键配置类代表项（完整文件列表可按 `@Configuration` 全量导出复核）：

- gulimail-common
  - [MybatisPlusConfig](file:///d:/GitProgram/gulimail/gulimail-common/src/main/java/com/lg/common/config/MybatisPlusConfig.java)
- gulimail-gateway
  - [SentinelGatewayConfig](file:///d:/GitProgram/gulimail/gulimail-gateway/src/main/java/com/lg/gulimail/gateway/config/SentinelGatewayConfig.java)
  - [GuliMailCorsConfiguration](file:///d:/GitProgram/gulimail/gulimail-gateway/src/main/java/com/lg/gulimail/gateway/config/GuliMailCorsConfiguration.java)
- gulimail-order
  - [MyMQConfig](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/MyMQConfig.java)
  - [MyRabbitConfig](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/MyRabbitConfig.java)
  - [MyRedissonConfig](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/MyRedissonConfig.java)
  - [AlipayTemplate](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/config/AlipayTemplate.java)
- gulimail-product
  - [RedisConfig](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/config/RedisConfig.java)
  - [MyCacheConfig](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/config/MyCacheConfig.java)
  - [FeignConfig](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/config/FeignConfig.java)
  - [MyBatisConfig](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/config/MyBatisConfig.java)
  - [ThreadPoolConfigProperties](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/config/ThreadPoolConfigProperties.java)
- gulimail-seckill
  - [MyMQConfig](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/config/MyMQConfig.java)
  - [MyRabbitConfig](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/config/MyRabbitConfig.java)
  - [MyRedissonConfig](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/config/MyRedissonConfig.java)
- gulimail-third-party
  - [OssConfig](file:///d:/GitProgram/gulimail/gulimail-third-party/src/main/java/com/lg/gulimail/thirdparty/config/OssConfig.java)

## 3. MyBatis-Plus Mapper 基线（Java 接口）

以 `extends BaseMapper<T>` 作为统计口径（不含 XML 文件）：

| 模块 | Mapper 数量 | 约定目录 |
| --- | ---:| --- |
| gulimail-product | 16 | `com.lg.gulimail.product.dao` |
| gulimail-coupon | 15 | `com.lg.gulimail.coupon.dao` |
| gulimail-member | 9 | `com.lg.gulimail.member.dao` |
| gulimail-order | 8 | `com.lg.gulimail.order.dao` |
| gulimail-ware | 6 | `com.lg.gulimail.ware.dao` |
| gulimail-ai | 2 | `com.lg.gulimail.ai.mapper` |

对应的 Mapper XML 位于各模块 `src/main/resources/mapper/**`（详见 [mapper 目录清单](file:///d:/GitProgram/gulimail) 下的匹配结果，可复核是否存在“手写 SQL”与“复杂查询”集中点）。

## 4. Feign Client 清单（@FeignClient）

自动扫描到 20 个 Feign Client（按服务依赖关系，能够直接反推“核心业务链路”的跨服务调用边）：

- gulimail-ai
  - [OrderFeignService](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/feign/OrderFeignService.java)
  - [ProductFeignService](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/feign/ProductFeignService.java)
  - [SeckillFeignService](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/feign/SeckillFeignService.java)
  - [WareFeignService](file:///d:/GitProgram/gulimail/gulimail-ai/src/main/java/com/lg/gulimail/ai/feign/WareFeignService.java)
- gulimail-auth-server
  - [MemberFeignService](file:///d:/GitProgram/gulimail/gulimail-auth-server/src/main/java/com/lg/gulimail/authserver/feign/MemberFeignService.java)
  - [ThirdPartyFeignService](file:///d:/GitProgram/gulimail/gulimail-auth-server/src/main/java/com/lg/gulimail/authserver/feign/ThirdPartyFeignService.java)
- gulimail-cart
  - [ProductFeignService](file:///d:/GitProgram/gulimail/gulimail-cart/src/main/java/com/lg/gulimail/cart/feign/ProductFeignService.java)
- gulimail-member
  - [CouponFeignService](file:///d:/GitProgram/gulimail/gulimail-member/src/main/java/com/lg/gulimail/member/feign/CouponFeignService.java)
- gulimail-order
  - [CartFeignService](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/feign/CartFeignService.java)
  - [MemberFeignService](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/feign/MemberFeignService.java)
  - [WmsFeignService](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/feign/WmsFeignService.java)
- gulimail-product
  - [CouponFeignService](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/feign/CouponFeignService.java)
  - [OrderFeignService](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/feign/OrderFeignService.java)
  - [SearchFeignService](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/feign/SearchFeignService.java)
  - [SeckillFeignService](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/feign/SeckillFeignService.java)
  - [WareFeignService](file:///d:/GitProgram/gulimail/gulimail-product/src/main/java/com/lg/gulimail/product/feign/WareFeignService.java)
- gulimail-seckill
  - [CouponFeignService](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/feign/CouponFeignService.java)
  - [ProductFeignService](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/feign/ProductFeignService.java)
- gulimail-ware
  - [MemberFeignService](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/feign/MemberFeignService.java)
  - [SkuInfoFeignService](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/feign/SkuInfoFeignService.java)

## 5. 定时任务清单（@Scheduled）

- gulimail-seckill
  - [SeckillSkeduler](file:///d:/GitProgram/gulimail/gulimail-seckill/src/main/java/com/lg/gulimail/seckill/scheduled/SeckillSkeduler.java)

## 6. 消息监听器清单（@RabbitListener）

自动扫描到 6 个 RabbitMQ 消息监听器：

- gulimail-order
  - [OrderReleaseListener](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/listener/OrderReleaseListener.java)（`order.release.order.queue`）
  - [OrderSeckillListener](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/listener/OrderSeckillListener.java)（`order.seckill.order.queue`）
  - [OrderCloseListener](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/listener/OrderCloseListener.java)（`order.seckill.release.queue`）
  - [SeckillStockRollbackListener](file:///d:/GitProgram/gulimail/gulimail-order/src/main/java/com/lg/gulimail/order/listener/SeckillStockRollbackListener.java)（`seckill.rollback.queue`）
- gulimail-ware
  - [StockDeductListener](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/listener/StockDeductListener.java)（`stock.deduct.queue`）
  - [StockReleaseListener](file:///d:/GitProgram/gulimail/gulimail-ware/src/main/java/com/lg/gulimail/ware/listener/StockReleaseListener.java)（`stock.release.stock.queue`）

