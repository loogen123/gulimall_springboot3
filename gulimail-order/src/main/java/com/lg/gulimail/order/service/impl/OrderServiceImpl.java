package com.lg.gulimail.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.to.OrderTo;
import com.lg.common.to.SeckillOrderTo;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.utils.R;
import com.lg.common.utils.RRException;
import com.lg.common.vo.FareVo;
import com.lg.common.vo.MemberAddressVo;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.OrderItemVo;
import com.lg.gulimail.order.constant.OrderConstant;
import com.lg.gulimail.order.constant.OrderStatusEnum;
import com.lg.gulimail.order.dao.OrderDao;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.entity.OrderItemEntity;
import com.lg.gulimail.order.entity.PaymentInfoEntity;
import com.lg.gulimail.order.exception.NoStockException;
import com.lg.gulimail.order.feign.CartFeignService;
import com.lg.gulimail.order.feign.CouponFeignService;
import com.lg.gulimail.order.feign.MemberFeignService;
import com.lg.gulimail.order.feign.WmsFeignService;
import com.lg.gulimail.order.interceptor.LoginUserInterceptor;
import com.lg.gulimail.order.service.OrderItemService;
import com.lg.gulimail.order.service.OrderService;
import com.lg.gulimail.order.service.PaymentInfoService;
import com.lg.gulimail.order.to.OrderCreateTo;
import com.lg.gulimail.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 从拦截器获取当前登录用户
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        // 1. 获取主线程请求属性（解决异步线程丢失请求上下文/Cookie问题）
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        // 2. 异步获取收货地址
        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            // 子线程同步上下文
            RequestContextHolder.setRequestAttributes(attributes);

            // 【最终修改点】：直接接收 List，因为 Feign 接口就是这么定义的
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        // 3. 异步获取购物车商品
        CompletableFuture<Void> cartTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor);

        // 4. 查询积分（直接从登录信息取）
        confirmVo.setIntegration(memberResponseVo.getIntegration());

        // 5. 等待所有异步任务完成
        CompletableFuture.allOf(addressTask, cartTask).get();

        // 6. 【新增】生成防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        // 存入 Redis，过期时间建议 30 分钟
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        // 返回给前端
        confirmVo.setOrderToken(token);

        return confirmVo;
    }
    private OrderCreateTo createOrder(OrderSubmitVo vo) {
        OrderCreateTo createTo = new OrderCreateTo();

        // 1. 生成全局唯一订单号（使用雪花算法）
        String orderSn = IdWorker.getIdStr();

        // 2. 构建订单主体记录
        OrderEntity orderEntity = buildOrder(vo, orderSn);
        createTo.setOrder(orderEntity);

        // 3. 构建所有订单项记录（从购物车获取勾选商品）
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        createTo.setOrderItems(itemEntities);

        // 4. 计算价格相关（验价逻辑的核心）
        computePrice(orderEntity, itemEntities, vo);

        return createTo;
    }
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 获取购物车中选中的商品项
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            return currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity item = buildOrderItem(cartItem);
                item.setOrderSn(orderSn);
                return item;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private OrderEntity buildOrder(OrderSubmitVo vo, String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(LoginUserInterceptor.loginUser.get().getId());

        // 1. 远程获取运费和地址信息
        R r = wmsFeignService.getFare(vo.getAddrId());
        // 使用 R 提供的 getData 方法解析出真实的 FareVo
        FareVo fareResp = r.getData(new TypeReference<FareVo>(){});

        // 2. 【核心修复】增加非空校验，防止 NPE
        if (fareResp == null || fareResp.getAddress() == null) {
            // 抛出自定义异常，让 OrderWebController 的 catch 块捕获并重定向回确认页
            throw new RRException("无法获取收货地址详细信息，请检查地址是否有效或联系管理员", BizCodeEnum.NOT_FOUND_EXCEPTION.getCode());
        }

        // 3. 安全地提取数据
        entity.setFreightAmount(fareResp.getFare());
        MemberAddressVo address = fareResp.getAddress();

        entity.setReceiverCity(address.getCity());
        entity.setReceiverDetailAddress(address.getDetailAddress());
        entity.setReceiverName(address.getName());
        entity.setReceiverPhone(address.getPhone());
        entity.setReceiverProvince(address.getProvince());
        entity.setReceiverRegion(address.getRegion());

        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        return entity;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity item = new OrderItemEntity();
        // 1. 商品SPU信息（根据SKU反查SPU）
        // 2. 商品SKU信息（标题、图片、属性、价格）
        item.setSkuId(cartItem.getSkuId());
        item.setSkuName(cartItem.getTitle());
        item.setSkuPrice(cartItem.getPrice());
        item.setSkuQuantity(cartItem.getCount());
        // ...
        return item;
    }
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities, OrderSubmitVo vo) {
        if (itemEntities == null || itemEntities.isEmpty()) {
            throw new RRException("未选中任何商品，无法提交订单", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemEntity item : itemEntities) {
            BigDecimal itemPrice = item.getSkuPrice().multiply(new BigDecimal(item.getSkuQuantity()));
            total = total.add(itemPrice);
        }
        BigDecimal freight = orderEntity.getFreightAmount() == null ? BigDecimal.ZERO : orderEntity.getFreightAmount();
        OrderBenefitQuoteVo benefitQuote = quoteBenefits(vo, total);
        BigDecimal couponAmount = benefitQuote.getCouponAmount() == null ? BigDecimal.ZERO : benefitQuote.getCouponAmount();
        BigDecimal integrationAmount = benefitQuote.getIntegrationAmount() == null ? BigDecimal.ZERO : benefitQuote.getIntegrationAmount();
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponId(benefitQuote.getCouponId());
        orderEntity.setUseIntegration(benefitQuote.getUseIntegration());
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(BigDecimal.ZERO);
        orderEntity.setDiscountAmount(BigDecimal.ZERO);
        BigDecimal payAmount = total.add(freight).subtract(couponAmount).subtract(integrationAmount);
        orderEntity.setPayAmount(payAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : payAmount);
    }

    private OrderBenefitQuoteVo quoteBenefits(OrderSubmitVo vo, BigDecimal total) {
        OrderBenefitQuoteVo quote = new OrderBenefitQuoteVo();
        if (vo == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return quote;
        }
        int requestIntegration = vo.getUseIntegration() == null ? 0 : vo.getUseIntegration();
        if (requestIntegration > 0) {
            Map<String, Object> request = new HashMap<>();
            request.put("memberId", LoginUserInterceptor.loginUser.get().getId());
            request.put("useIntegration", requestIntegration);
            request.put("orderTotal", total);
            R integrationResp = memberFeignService.quoteIntegration(request);
            if (integrationResp.getCode() != 0) {
                throw new RRException((String) integrationResp.get("msg"), BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            Integer useIntegration = integrationResp.getData("useIntegration", new TypeReference<Integer>() {});
            BigDecimal integrationAmount = integrationResp.getData("integrationAmount", new TypeReference<BigDecimal>() {});
            quote.setUseIntegration(useIntegration == null ? 0 : useIntegration);
            quote.setIntegrationAmount(integrationAmount == null ? BigDecimal.ZERO : integrationAmount);
        }
        if (vo.getCouponId() != null) {
            R couponResp = couponFeignService.getCouponInfo(vo.getCouponId());
            if (couponResp.getCode() != 0) {
                throw new RRException("优惠券不存在或不可用", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            CouponInfoVo coupon = couponResp.getData("coupon", new TypeReference<CouponInfoVo>() {});
            if (coupon == null || coupon.getId() == null) {
                throw new RRException("优惠券不存在或不可用", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            Date now = new Date();
            if (coupon.getPublish() != null && coupon.getPublish() == 0) {
                throw new RRException("优惠券未发布", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            if (coupon.getStartTime() != null && now.before(coupon.getStartTime())) {
                throw new RRException("优惠券未到生效时间", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            if (coupon.getEndTime() != null && now.after(coupon.getEndTime())) {
                throw new RRException("优惠券已过期", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            if (coupon.getMinPoint() != null && total.compareTo(coupon.getMinPoint()) < 0) {
                throw new RRException("订单金额未达到优惠券门槛", BizCodeEnum.VAILD_EXCEPTION.getCode());
            }
            BigDecimal couponAmount = coupon.getAmount() == null ? BigDecimal.ZERO : coupon.getAmount();
            if (couponAmount.compareTo(BigDecimal.ZERO) < 0) {
                couponAmount = BigDecimal.ZERO;
            }
            BigDecimal maxCouponAmount = total.subtract(quote.getIntegrationAmount());
            if (couponAmount.compareTo(maxCouponAmount) > 0) {
                couponAmount = maxCouponAmount;
            }
            quote.setCouponId(coupon.getId());
            quote.setCouponAmount(couponAmount);
        }
        return quote;
    }
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        // 1. 获取当前登录用户
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        // 2. 查询订单时，必须带上 member_id 条件，防止越权访问
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>()
                .eq("order_sn", orderSn)
                .eq("member_id", member.getId()));

        return order;
    }
    /**
     * 保存订单数据到数据库
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        // 1. 获取订单主体
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        // 保存订单主表 oms_order
        this.save(order);

        // 2. 获取订单项列表
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        // 批量保存订单项表 oms_order_item
        // 注意：这里需要确保 orderItemService 已经注入
        orderItemService.saveBatch(orderItems);
    }
    @io.seata.spring.annotation.GlobalTransactional // 开启分布式事务
    @Transactional // 开启本地事务
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        response.setCode(0); // 默认成功

        // 1. 从拦截器获取当前用户信息
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        // 2. 验证令牌（使用 Lua 脚本保证原子性）
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()),
                vo.getOrderToken());

        if (result != null && result == 1) {
            // 令牌验证通过，执行下单

            // 3. 构造订单数据（调用咱们写的 createOrder）
            OrderCreateTo order = createOrder(vo);

            // 4. 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {

                // 5. 保存订单到数据库（调用咱们写的 saveOrder）
                saveOrder(order);

                // 6. 远程调用库存服务锁定库存
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                // 将订单项转化为锁库存需要的 VO（由于你之前定义过，直接转换即可）
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);

                // 【核心】远程锁库存
                // R 是你们项目通用的返回数据封装
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
                    response.setOrder(order.getOrder());

                    // TODO: 7. 下单成功，给 MQ 发送消息（用于延迟关单）
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    return response;
                } else {
                    // 锁定失败：抛出异常让 Spring 感知并回滚【saveOrder】保存的订单数据
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            } else {
                // 4. 验价失败
                response.setCode(2);
                return response;
            }
        } else {
            // 2. 令牌验证失败
            response.setCode(1);
            return response;
        }
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        int rows = this.baseMapper.updateOrderStatusByIdAndFromStatus(
                entity.getId(),
                OrderStatusEnum.CANCELED.getCode(),
                OrderStatusEnum.CREATE_NEW.getCode()
        );
        if (rows > 0) {
            OrderEntity latest = this.getById(entity.getId());
            OrderEntity closedOrder = latest == null ? entity : latest;
            revertMemberIntegrationIfNeeded(closedOrder);
            revertCouponIfNeeded(closedOrder);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    // 在 OrderServiceImpl 中实现
    @Override
    public PayVo getOrderPay(String orderSn) {
        // 1. 根据订单号查询订单实体
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        // 2. 封装 PayVo
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);
        // 支付宝要求金额必须是字符串，且保留两位小数
        payVo.setTotal_amount(order.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setSubject("谷粒商城订单：" + orderSn);
        payVo.setBody(order.getNote());

        return payVo;
    }
    @Autowired
    private PaymentInfoService paymentInfoService;
    // 在 OrderServiceImpl 中添加
    @Override
    public void handlePayResult(PayAsyncVo vo) {
        // 1. 保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCreateTime(new Date());
        infoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        paymentInfoService.save(infoEntity);

        // 2. 修改订单状态
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            String outTradeNo = vo.getOut_trade_no();

            // 核心修改：通过返回值判断幂等性，防止重复发送 MQ
            int rows = this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());

            if (rows > 0) {
                OrderEntity order = this.getOrderBySn(outTradeNo);
                deductMemberIntegrationIfNeeded(order);
                deductCouponIfNeeded(order);
                // 3. 【核心修改】发送 OrderTo 而不是 OrderEntity
                OrderTo orderTo = new OrderTo();
                orderTo.setOrderSn(outTradeNo);

                // 发送给库存服务执行真正的“扣减”
                rabbitTemplate.convertAndSend("order-event-exchange", "order.deduct.stock", orderTo);

                log.info("订单：{} 支付成功，已向 MQ 发送 OrderTo 库存扣减指令", outTradeNo);
            } else {
                log.warn("订单：{} 状态已处理，跳过 MQ 发送", outTradeNo);
            }
        }
    }

    private void deductMemberIntegrationIfNeeded(OrderEntity order) {
        if (order == null || order.getMemberId() == null || order.getUseIntegration() == null || order.getUseIntegration() <= 0) {
            return;
        }
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", order.getMemberId());
        request.put("orderSn", order.getOrderSn());
        request.put("useIntegration", order.getUseIntegration());
        R r = memberFeignService.deductIntegration(request);
        if (r.getCode() != 0) {
            log.error("订单：{} 积分核销失败，msg={}", order.getOrderSn(), r.get("msg"));
        }
    }

    private void revertMemberIntegrationIfNeeded(OrderEntity order) {
        if (order == null || order.getMemberId() == null || order.getUseIntegration() == null || order.getUseIntegration() <= 0) {
            return;
        }
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", order.getMemberId());
        request.put("orderSn", order.getOrderSn());
        request.put("useIntegration", order.getUseIntegration());
        R r = memberFeignService.revertIntegration(request);
        if (r.getCode() != 0) {
            log.error("订单：{} 积分回滚失败，msg={}", order.getOrderSn(), r.get("msg"));
        }
    }

    private void deductCouponIfNeeded(OrderEntity order) {
        if (order == null || order.getMemberId() == null || order.getCouponId() == null || order.getCouponId() <= 0) {
            return;
        }
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", order.getMemberId());
        request.put("orderSn", order.getOrderSn());
        request.put("orderId", order.getId());
        request.put("couponId", order.getCouponId());
        R r = couponFeignService.deductCoupon(request);
        if (r.getCode() != 0) {
            log.error("订单：{} 优惠券核销失败，msg={}", order.getOrderSn(), r.get("msg"));
        }
    }

    private void revertCouponIfNeeded(OrderEntity order) {
        if (order == null || order.getMemberId() == null || order.getCouponId() == null || order.getCouponId() <= 0) {
            return;
        }
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", order.getMemberId());
        request.put("orderSn", order.getOrderSn());
        request.put("orderId", order.getId());
        request.put("couponId", order.getCouponId());
        R r = couponFeignService.revertCoupon(request);
        if (r.getCode() != 0) {
            log.error("订单：{} 优惠券回滚失败，msg={}", order.getOrderSn(), r.get("msg"));
        }
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        params.putIfAbsent("limit", "10");
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberResponseVo.getId())
                        .orderByDesc("id")
        );
        List<OrderEntity> orders = page.getRecords();
        if (orders == null || orders.isEmpty()) {
            return new PageUtils(page);
        }
        List<String> orderSnList = orders.stream()
                .map(OrderEntity::getOrderSn)
                .collect(Collectors.toList());
        List<OrderItemEntity> allOrderItems = orderItemService.list(
                new QueryWrapper<OrderItemEntity>().in("order_sn", orderSnList)
        );
        Map<String, List<OrderItemEntity>> itemsByOrderSn = allOrderItems.stream()
                .collect(Collectors.groupingBy(OrderItemEntity::getOrderSn));
        orders.forEach(order -> order.setItemEntities(
                itemsByOrderSn.getOrDefault(order.getOrderSn(), Collections.emptyList())
        ));
        page.setRecords(orders);

        return new PageUtils(page);
    }

    @Override
    public OrderEntity getOrderWithDetailsByOrderSn(String orderSn) {
        // 调用上面的基础查询
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        if (order != null) {
            // 只有在明确需要详情时，才去查订单项
            List<OrderItemEntity> itemEntities = orderItemService.list(
                    new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
            order.setItemEntities(itemEntities);
        }
        return order;
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        log.info("秒杀订单开始入库：{}", seckillOrder.getOrderSn());

        try {
            // 1. 保存订单主表信息 (oms_order)
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setOrderSn(seckillOrder.getOrderSn());
            orderEntity.setMemberId(seckillOrder.getMemberId());

            // --- 补充可能导致报错的必填字段 ---
            orderEntity.setCreateTime(new Date()); // 很多表这个字段不能为空
            orderEntity.setModifyTime(new Date());
            orderEntity.setConfirmStatus(0); // 确认状态
            orderEntity.setDeleteStatus(0);  // 删除状态
            // ------------------------------

            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

            BigDecimal payAmount = seckillOrder.getSeckillPrice().multiply(new BigDecimal(seckillOrder.getNum()));
            orderEntity.setPayAmount(payAmount);
            orderEntity.setTotalAmount(payAmount);

            // 执行保存
            this.save(orderEntity);

            // 2. 保存订单项信息 (oms_order_item)
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setOrderSn(seckillOrder.getOrderSn());
            itemEntity.setRealAmount(payAmount);
            itemEntity.setSkuId(seckillOrder.getSkuId());
            itemEntity.setSkuQuantity(seckillOrder.getNum());

            // --- 同样的，补充可能导致报错的必填字段 ---
            // 如果 OrderItem 也有非空约束，请在这里设置
            // --------------------------------------

            orderItemService.save(itemEntity);

            log.info("秒杀订单入库成功：{}", seckillOrder.getOrderSn());

        } catch (Exception e) {
            log.error("！！！秒杀订单数据库写入失败！！！原因：{}", e.getMessage());
            // 必须抛出异常，让 Spring 感知到回滚，同时在 Listener 中触发 Reject
            throw e;
        }

    }

    @Override
    public OrderEntity getOrderBySn(String orderSn) {
        // 使用 MyBatis Plus 的 LambdaQuery 查询
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

}
