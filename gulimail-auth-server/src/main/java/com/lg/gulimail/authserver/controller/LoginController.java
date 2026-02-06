package com.lg.gulimail.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.feign.MemberFeignService;
import com.lg.gulimail.authserver.feign.ThirdPartyFeignService;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 1. 登录页跳转
     */
    @GetMapping({"/", "/login.html"})
    public String loginPage(HttpSession session) {
        // 如果已经登录，直接跳首页，防止重复登录
        if (session.getAttribute("loginUser") != null) {
            return "redirect:http://gulimail.com";
        }
        return "login";
    }

    /**
     * 2. 注册页跳转
     */
    @GetMapping("/reg.html")
    public String regPage() {
        return "reg";
    }

    /**
     * 3. 核心：提交注册逻辑 (新增部分)
     */
    @ResponseBody // 关键：返回 JSON 而不是页面跳转
    @PostMapping("/register")
    public R register(@Valid UserRegisterVo vo, BindingResult result) {

        // ① 数据校验（JSR303）
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1));
            // AJAX 模式下直接把 Map 返回给前端
            return R.error(400, "数据校验失败").put("errors", errors);
        }

        // ② 校验验证码
        R r = thirdPartyFeignService.checkCode(vo.getPhone(), vo.getCode());
        if (r.getCode() != 0) {
            // 构造一个包含 code 键的错误 Map，方便前端精准定位到验证码输入框
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误或已过期");
            return R.error(400, "验证码校验失败").put("errors", errors);
        }

        // ③ 调用 Member 服务进行存库
        R register = memberFeignService.register(vo);
        if (register.getCode() == 0) {
            // 注册成功，返回 R.ok()，由前端 JS 决定跳转到登录页
            return R.ok();
        } else {
            // 注册失败（如用户名已存在），返回后端传回的具体错误原因
            String msg = (String) register.get("msg");
            return R.error(400, msg != null ? msg : "注册失败，请稍后再试");
        }
    }
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 远程调用你的 third-party 服务
        return thirdPartyFeignService.sendCode(phone);
    }

    @ResponseBody // 必须加这个，告诉 Spring 返回 JSON 而不是页面
    @PostMapping("/login")
    public R login(UserLoginVo vo, HttpSession session) {

        // 1. 远程调用 Member 服务进行验证
        R r = memberFeignService.login(vo);

        if (r.getCode() == 0) {
            // 2. 登录成功：把用户信息存入 Session
            MemberResponseVo data = r.getData("member", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);

            // 返回成功状态，让前端 JS 决定去哪
            return R.ok();
        } else {
            // 3. 登录失败：把错误消息传给前端展示
            String msg = (String) r.get("msg");
            return R.error(msg);
        }
    }
    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 1. 彻底销毁 Session（包括 Redis 里的数据）
        if (session != null) {
            session.removeAttribute(AuthServerConstant.LOGIN_USER);
            session.invalidate();
        }

        // 2. 这里的地址建议配置在配置文件中，方便维护
        return "redirect:http://gulimail.com";
    }
}