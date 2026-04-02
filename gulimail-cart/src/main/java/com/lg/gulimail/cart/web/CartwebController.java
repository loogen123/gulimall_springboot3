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

    private static final String LOGIN_URL = "http://auth.gulimail.com/login.html";
    private static final String CART_LIST_URL = "redirect:http://cart.gulimail.com/cartList.html";

    @Autowired
    CartService cartService;

    /**
     * 购物车列表页
     */
    @GetMapping({"/", "/cartList.html"})
    public String cartListPage(HttpSession session, Model model) throws ExecutionException, InterruptedException {
        MemberResponseVo member = getLoginUser(session);
        if (member == null) {
            return "redirect:" + LOGIN_URL;
        }
        Cart cart = cartService.getCart(member.getId());
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
        MemberResponseVo member = getLoginUser(session);
        if (member == null) {
            return "redirect:" + LOGIN_URL;
        }
        if (skuId == null || skuId < 1 || num == null || num < 1) {
            return CART_LIST_URL;
        }
        cartService.addToCart(skuId, num, member.getId());
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimail.com/addToCartSuccess.html";
    }

    /**
     * 添加成功跳转页
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, HttpSession session, Model model) {
        MemberResponseVo member = getLoginUser(session);
        model.addAttribute("member", member);
        Long userId = (member != null) ? member.getId() : null;
        CartItem cartItem = cartService.getCartItem(skuId, userId);
        model.addAttribute("skuInfo", cartItem);
        model.addAttribute("skuNum", (cartItem != null) ? cartItem.getCount() : 0);
        return "success";
    }
    /**
     * 删除购物车商品
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId, HttpSession session) {
        MemberResponseVo user = getLoginUser(session);
        if (user == null) {
            return "redirect:" + LOGIN_URL;
        }
        if (skuId == null || skuId < 1) {
            return CART_LIST_URL;
        }
        Long userId = user.getId();
        cartService.deleteItem(skuId, userId);
        return CART_LIST_URL;
    }
    
    @ResponseBody
    @GetMapping("/updateCount")
    public CartItem updateCount(@RequestParam("skuId") Long skuId,
                                @RequestParam("num") Integer num,
                                HttpSession session) throws ExecutionException, InterruptedException {
        MemberResponseVo user = getLoginUser(session);
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        if (skuId == null || skuId < 1) {
            throw new IllegalArgumentException("skuId参数非法");
        }
        if (num == null || num < 1) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        Long userId = user.getId();
        return cartService.addToCart(skuId, num, userId);
    }

    private MemberResponseVo getLoginUser(HttpSession session) {
        return (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
    }
}
