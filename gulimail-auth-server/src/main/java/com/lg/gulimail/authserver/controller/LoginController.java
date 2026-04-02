package com.lg.gulimail.authserver.controller;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.application.auth.LoginApplicationService;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {
    private final LoginApplicationService loginApplicationService;

    @Value("${gulimail.auth.oauth.github.authorize-url:https://github.com/login/oauth/authorize}")
    private String githubAuthorizeUrl;

    @Value("${gulimail.auth.oauth.github.client-id:}")
    private String githubClientId;

    @Value("${gulimail.auth.oauth.github.redirect-uri:http://auth.gulimail.com/oauth2.0/github/success}")
    private String githubRedirectUri;

    @Value("${gulimail.auth.login-success-redirect:http://gulimail.com}")
    private String loginSuccessRedirect;

    public LoginController(LoginApplicationService loginApplicationService) {
        this.loginApplicationService = loginApplicationService;
    }

    /**
     * 1. 登录页跳转
     */
    @GetMapping({"/", "/login.html"})
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute(AuthServerConstant.LOGIN_USER) != null) {
            return "redirect:" + loginSuccessRedirect;
        }
        String githubAuthUrl = loginApplicationService.prepareGithubAuthUrl(session, githubAuthorizeUrl, githubClientId, githubRedirectUri);
        model.addAttribute("githubAuthUrl", githubAuthUrl);
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
    @ResponseBody
    @PostMapping("/register")
    public R register(@Valid UserRegisterVo vo, BindingResult result) {
        return loginApplicationService.register(vo, result);
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        return loginApplicationService.sendCode(phone);
    }

    @ResponseBody
    @PostMapping("/login")
    public R login(@Valid UserLoginVo vo, BindingResult result, HttpSession session) {
        return loginApplicationService.login(vo, result, session);
    }
    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        loginApplicationService.logout(session);
        return "redirect:" + loginSuccessRedirect;
    }
}
