package com.lg.gulimail.order.web;

import com.alipay.api.AlipayApiException;
import com.lg.common.utils.PageUtils;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.order.application.order.SubmitOrderApplicationService;
import com.lg.gulimail.order.config.AlipayTemplate;
import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.interceptor.LoginUserInterceptor;
import com.lg.gulimail.order.service.OrderService;
import com.lg.gulimail.order.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
public class OrderWebController {
    private static final int MAX_PAGE_NUM = 100;
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    /**
     * 订单确认页
     */
    @Autowired
    OrderService orderService;
    @Autowired
    SubmitOrderApplicationService submitOrderApplicationService;

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
            OrderSubmitResult responseVo = submitOrderApplicationService.submitOrder(OrderSubmitCommand.from(vo));

            if (responseVo.isSuccess()) {
                return "redirect:http://order.gulimail.com/pay.html?orderSn=" + responseVo.getOrderSn();
            } else {
                redirectAttributes.addFlashAttribute("msg", responseVo.message());
                return "redirect:http://order.gulimail.com/confirm.html";
            }
        } catch (Exception e) {
            log.error("提交订单异常", e);
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
        if (vo == null || isBlank(vo.getOut_trade_no())
                || isBlank(vo.getTrade_status())
                || isBlank(vo.getTotal_amount())
                || isBlank(vo.getTrade_no())) {
            return "error";
        }
        String tradeStatus = vo.getTrade_status().trim();
        if (!TRADE_SUCCESS.equals(tradeStatus) && !TRADE_FINISHED.equals(tradeStatus)) {
            return "error";
        }
        vo.setOut_trade_no(vo.getOut_trade_no().trim());
        vo.setTrade_status(tradeStatus);
        vo.setTotal_amount(vo.getTotal_amount().trim());
        vo.setTrade_no(vo.getTrade_no().trim());
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
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        } else if (pageNum > MAX_PAGE_NUM) {
            pageNum = MAX_PAGE_NUM;
        }
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

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
