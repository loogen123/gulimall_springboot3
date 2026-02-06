package com.lg.gulimail.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.authserver.feign.MemberFeignService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/oauth2.0/github/success")
    public String github(@RequestParam("code") String code, HttpSession session) {
        // 1. 换取 Token 的接口地址
        String url = "https://github.com/login/oauth/access_token";

        // 2. 配置 Header（必须要求返回 JSON）
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. 封装请求参数
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "Ov23livWv1rMiXLSFkIz");
        map.add("client_secret", "e6fe3dedad338cd5a94d34bb5b7a635a51d46d0d");
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", "http://auth.gulimail.com/oauth2.0/github/success");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            // 4. POST 请求获取 AccessToken
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String body = response.getBody();
                log.info("GitHub 换取 AccessToken 原始数据: {}", body);

                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(body);
                String accessToken = jsonObject.getString("access_token");

                if (accessToken != null && !"".equals(accessToken)) {
                    SocialUser socialUser = new SocialUser();
                    socialUser.setAccessToken(accessToken);

                    // 5. 远程调用 Member 服务进行登录/注册
                    R r = memberFeignService.oauthLogin(socialUser);
                    if (r.getCode() == 0) {
                        // 【关键修复】这里将 "data" 改为 "member"，匹配你 MemberController 的 put("member", entity)
                        MemberResponseVo data = r.getData("member", new TypeReference<MemberResponseVo>() {});

                        // 【增加安全性】先判断 data 是否为空，再获取 nickname
                        if (data != null) {
                            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                            log.info("GitHub 用户登录成功，欢迎：{}", data.getNickname());
                            // 登录成功，跳转到商城首页
                            return "redirect:http://gulimail.com";
                        } else {
                            log.error("远程调用成功，但未能在响应中找到 Key 为 'member' 的数据");
                        }
                    } else {
                        log.error("Member 服务返回错误状态码：{}", r.getCode());
                    }
                }
            }
        } catch (Exception e) {
            log.error("GitHub 授权流程异常：", e);
        }

        // 失败（或发生异常）则重定向回登录页
        return "redirect:http://auth.gulimail.com/login.html";
    }
}