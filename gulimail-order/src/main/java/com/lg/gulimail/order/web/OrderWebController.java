package com.lg.gulimail.order.web;

import com.alipay.api.AlipayApiException;
import com.lg.common.utils.PageUtils;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.order.config.AlipayTemplate;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.interceptor.LoginUserInterceptor;
import com.lg.gulimail.order.service.OrderService;
import com.lg.gulimail.order.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    /**
     * 订单确认页
     */
    @Autowired
    OrderService orderService;

    @GetMapping("/confirm.html")
    public String confirmPage(Model model) throws ExecutionException, InterruptedException {
        // 1. 获取当前登录用户（拦截器已放入 ThreadLocal）【写了拦截器之后不用在此获取用户信息】
        MemberResponseVo user = LoginUserInterceptor.loginUser.get();

        // 2. 调用 Service 进行多服务异步数据聚合
        // 注意：这里需要抛出或捕获异步执行异常
        OrderConfirmVo confirmVo = orderService.confirmOrder();

        // 3. 将聚合好的数据传递给 Thymeleaf 页面
        model.addAttribute("orderConfirmData", confirmVo);

        return "confirm";
    }

    /**
     * 订单详情页
     */
    @GetMapping("/orderDetail.html")
    public String orderDetail(@RequestParam("orderSn") String orderSn, Model model) {
        // 改用这个带明细的方法，这样页面上的 th:each="item : ${order.itemEntities}" 才有数据
        OrderEntity order = orderService.getOrderWithDetailsByOrderSn(orderSn);
        model.addAttribute("order", order);
        return "detail.html";
    }

    /**
     * 去支付页（收银台）
     */
    @GetMapping("/pay.html")
    public String payPage(@RequestParam("orderSn") String orderSn, Model model) {
        // 1. 根据订单号查询订单，获取应付金额等信息
        OrderEntity order = orderService.getOrderByOrderSn(orderSn);

        // 2. 传给页面显示（比如单号、应付总额）
        model.addAttribute("order", order);

        return "pay"; // 对应 templates/pay.html
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

            if (responseVo.getCode() == 0) {
                // 1. 获取刚刚下单成功的订单号
                String orderSn = responseVo.getOrder().getOrderSn();
                // 2. 重定向到详情页映射，并拼接订单号参数
                // 注意：必须用 redirect: 这样地址栏才会从 submitOrder 变成 detail.html
                return "redirect:http://order.gulimail.com/pay.html?orderSn=" + orderSn;
            } else {
                // 失败逻辑保持不变
                String msg = "下单失败";
                if(responseVo.getCode() == 1) msg = "订单信息过期，请刷新再试";
                if(responseVo.getCode() == 2) msg = "价格发生变化，请确认后再次提交";
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimail.com/confirm.html";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:http://order.gulimail.com/confirm.html";
        }
    }
    @Autowired
    AlipayTemplate alipayTemplate;
    /**
     * 这里的 produces = "text/html" 非常重要
     * 告诉浏览器返回的是 HTML 页面，浏览器会自动解析并跳转
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        // 1. 获取当前订单的支付数据
        PayVo payVo = orderService.getOrderPay(orderSn);

        // 2. 调用我们之前写的 alipayTemplate（它会读取 Nacos 里的密钥）
        // 返回的是一个自动提交的 <form> 表单字符串
        String result = alipayTemplate.pay(payVo);

        return result;
    }
    @ResponseBody
    @PostMapping(value = {"/payed/notify", "/order/payed/notify", "/api/order/payed/notify"})
    public String handleAlipayPost(PayAsyncVo vo, HttpServletRequest request) {
        orderService.handlePayResult(vo);
        return "success";
    }
    /**
     * 订单列表页
     * 修改：删掉原来的 listPage，让 list.html 映射也走到分页逻辑
     */
    @GetMapping({"/list.html", "/memberOrder.html"}) // 让两个路径都走这个逻辑
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  Model model) {
        // 1. 构造查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum.toString());
        params.put("limit", "10");
        // 2. 查询数据
        PageUtils page = orderService.queryPageWithItem(params);

        // 3. 这里的 key "orders" 必须和 list.html 里的 ${orders.list} 对应
        model.addAttribute("orders", page);

        return "list";
    }
}