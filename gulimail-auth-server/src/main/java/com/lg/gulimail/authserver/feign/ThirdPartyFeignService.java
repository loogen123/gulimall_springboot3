package com.lg.gulimail.authserver.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimail-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone);
    
    @PostMapping("/sms/checkCode")
    R checkCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}