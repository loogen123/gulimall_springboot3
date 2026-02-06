package com.lg.gulimail.member.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.member.entity.MemberReceiveAddressEntity;
import com.lg.gulimail.member.interceptor.LoginUserInterceptor;
import com.lg.gulimail.member.service.MemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 会员收货地址
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 18:11:41
 */
@RestController
@RequestMapping("member/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;
    /**
     * 获取指定用户的收货地址列表
     * 路径：/member/memberreceiveaddress/address/{memberId}
     */
    @GetMapping("/address/{memberId}")
    public List<MemberReceiveAddressEntity> getAddress(@PathVariable("memberId") Long memberId) {

        // 调用 Service 查询数据库中的地址表
        return memberReceiveAddressService.getAddress(memberId);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:memberreceiveaddress:info")
    public R info(@PathVariable("id") Long id){
		MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);

        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
        // 1. 从拦截器里拿当前用户的 ID
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        if(member != null){
            memberReceiveAddress.setMemberId(member.getId());
            memberReceiveAddressService.save(memberReceiveAddress);
            return R.ok();
        }
        return R.error("用户未登录，无法保存地址");
    }
    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
