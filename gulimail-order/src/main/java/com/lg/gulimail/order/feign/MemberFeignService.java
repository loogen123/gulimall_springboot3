package com.lg.gulimail.order.feign;

import com.lg.common.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimail-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/address/{memberId}")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}