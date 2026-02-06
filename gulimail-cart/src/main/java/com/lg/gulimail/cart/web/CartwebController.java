package com.lg.gulimail.cart.web;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.cart.service.CartService;
import com.lg.gulimail.cart.vo.Cart;
import com.lg.gulimail.cart.vo.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class CartwebController {

    @Autowired
    CartService cartService;

    /**
     * 购物车列表页
     */
    @GetMapping({"/", "/cartList.html"})
    public String cartListPage(HttpSession session, Model model) throws ExecutionException, InterruptedException {
        // 1. 获取当前登录用户
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (member == null) {
            // 未登录则跳转登录页
            return "redirect:http://auth.gulimail.com/login.html";
        }

        // 2. 获取该用户的购物车详情
        Cart cart = cartService.getCart(member.getId());

        // 3. 将数据放入 Model，前端 th:each="cartInfo:${cartList}" 才能拿到数据
        // 注意：你前端 HTML 写的是 cartList，所以 key 要对应
        model.addAttribute("cartList", cart.getItems());
        model.addAttribute("userId", member.getId());

        return "cartList";
    }
    /**
     * 添加商品到购物车
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            HttpSession session,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        // 1. 获取登录用户信息
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        // 2. 判定：如果为空说明 Session 没同步或没登录
        if (member == null) {
            return "redirect:http://auth.gulimail.com/login.html";
        }

        // 3. 调用 Service 存入 Redis
        cartService.addToCart(skuId, num, member.getId());

        // 4. 将 skuId 传给成功页，防止刷新页面重复添加
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimail.com/addToCartSuccess.html";
    }

    /**
     * 添加成功跳转页
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, HttpSession session, Model model) {
        // 从 Session 中获取登录用户信息
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        // 1. 将 member 放入 model，供顶部状态栏使用
        // 注意：如果 member 为 null（未登录），前端会走 th:if="${member == null}" 逻辑
        model.addAttribute("member", member);

        // 2. 获取刚添加的购物车项
        // 提醒：如果是未登录状态，member.getId() 会报空指针，这里建议先做判断或使用拦截器提供的 ID
        Long userId = (member != null) ? member.getId() : null;
        CartItem cartItem = cartService.getCartItem(skuId, userId);

        // 3. 适配前端变量名
        model.addAttribute("skuInfo", cartItem);
        model.addAttribute("skuNum", (cartItem != null) ? cartItem.getCount() : 0);

        return "success";
    }
    /**
     * 删除购物车商品
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId, HttpSession session) {
        // 这里你需要想办法拿到 userId，比如从 Session 里拿
        // 假设你的用户信息存在 session 的 "loginUser" 中
        MemberResponseVo user = (MemberResponseVo) session.getAttribute("loginUser");
        Long userId = user.getId();

        cartService.deleteItem(skuId, userId);
        return "redirect:http://cart.gulimail.com/cartList.html";
    }
    // 在 CartController.java 中
    @ResponseBody // 关键：不跳转页面，直接返回数据给 JS
    @GetMapping("/updateCount")
    public CartItem updateCount(@RequestParam("skuId") Long skuId,
                                @RequestParam("num") Integer num,
                                HttpSession session) throws ExecutionException, InterruptedException {

        // 1. 获取当前用户 ID (沿用你之前的逻辑)
        MemberResponseVo user = (MemberResponseVo) session.getAttribute("loginUser");
        Long userId = user.getId();

        // 2. 调用你的 Service，它会修改 Redis 并返回最新的 CartItem
        CartItem cartItem = cartService.addToCart(skuId, num, userId);

        // 3. 直接返回对象，SpringMVC 会自动将其转为 JSON
        return cartItem;
    }
}