package com.lg.gulimail.order.feign;

import com.lg.common.utils.R;
import com.lg.common.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("gulimail-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/address/{memberId}")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

    @PostMapping("/member/member/internal/integration/quote")
    R quoteIntegration(@RequestBody Map<String, Object> request);

    @PostMapping("/member/member/internal/integration/deduct")
    R deductIntegration(@RequestBody Map<String, Object> request);

    @PostMapping("/member/member/internal/integration/revert")
    R revertIntegration(@RequestBody Map<String, Object> request);
}
