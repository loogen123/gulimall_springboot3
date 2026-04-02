package com.lg.gulimail.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lg.common.utils.R;
import com.lg.gulimail.cart.feign.ProductFeignService;
import com.lg.gulimail.cart.interceptor.CartInterceptor;
import com.lg.gulimail.cart.service.CartService;
import com.lg.gulimail.cart.to.UserInfoTo;
import com.lg.gulimail.cart.vo.Cart;
import com.lg.gulimail.cart.vo.CartItem;
import com.lg.gulimail.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimail:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num, Long userId) throws ExecutionException, InterruptedException {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);

        String res = (String) cartOps.get(skuId.toString());

        if (!StringUtils.hasText(res)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                if (r != null && r.getCode() == 0) {
                    SkuInfoVo data = r.getData("skuInfo", new TypeReference<SkuInfoVo>(){});
                    if (data == null) {
                        throw new IllegalStateException("商品信息为空，skuId=" + skuId);
                    }
                    cartItem.setCheck(true);
                    cartItem.setCount(num);
                    cartItem.setImage(data.getSkuDefaultImg());
                    cartItem.setTitle(data.getSkuTitle());
                    cartItem.setSkuId(skuId);
                    cartItem.setPrice(data.getPrice());
                } else {
                    log.error("远程调用 getSkuInfo 失败，skuId: {}, 结果: {}", skuId, r);
                }
            }, executor);

            CompletableFuture<Void> getSkuAttrTask = CompletableFuture.runAsync(() -> {
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                if (attrValues != null) {
                    cartItem.setSkuAttr(attrValues);
                } else {
                    log.error("远程调用 getSkuSaleAttrValues 失败，skuId: {}，返回为 null", skuId);
                }
            }, executor);
            CompletableFuture.allOf(getSkuInfoTask, getSkuAttrTask).get();
            if (cartItem.getSkuId() == null) {
                throw new IllegalStateException("添加购物车失败，商品不存在，skuId=" + skuId);
            }
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId, Long userId) {
        if (userId == null) {
            return null;
        }
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        String res = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(res, CartItem.class);
    }

    @Override
    public Cart getCart(Long userId) {
        Cart cart = new Cart();
        String cartKey = CART_PREFIX + userId;
        List<Object> values = redisTemplate.opsForHash().values(cartKey);
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
            cart.setItems(collect);
        }
        return cart;
    }

    @Override
    public void deleteItem(Long skuId, Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo == null) return Collections.emptyList();
        String cartKey = CART_PREFIX + userInfoTo.getUserId();
        List<CartItem> cartItems = getCartItems(cartKey);
        if (cartItems != null) {
            return cartItems.stream().filter(CartItem::getCheck).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo == null) {
            return;
        }
        String cartKey = CART_PREFIX + userInfoTo.getUserId();
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        String json = (String) cartOps.get(skuId.toString());
        if (json != null) {
            CartItem cartItem = JSON.parseObject(json, CartItem.class);
            cartItem.setCheck(check == 1);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && !values.isEmpty()) {
            return values.stream().map(obj -> JSON.parseObject((String) obj, CartItem.class)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
