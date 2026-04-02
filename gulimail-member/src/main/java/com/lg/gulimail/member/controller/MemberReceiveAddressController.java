package com.lg.gulimail.member.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.common.utils.RRException;
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
    private static final int CODE_UNAUTHORIZED = 10002;
    private static final int CODE_FORBIDDEN = 10003;
    private static final String MSG_UNAUTHORIZED = "用户未登录";
    private static final String MSG_FORBIDDEN = "无访问权限";

    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;
    /**
     * 获取指定用户的收货地址列表
     * 路径：/member/memberreceiveaddress/address/{memberId}
     */
    @GetMapping("/address/{memberId}")
    public List<MemberReceiveAddressEntity> getAddress(@PathVariable("memberId") Long memberId) {
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if (member == null) {
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        if (!member.getId().equals(memberId)) {
            throw new RRException(MSG_FORBIDDEN, CODE_FORBIDDEN);
        }
        return memberReceiveAddressService.getAddress(member.getId());
    }
    /**
     * 列表 (仅查询当前登录用户自己的地址)
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if (member == null) {
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        params.put("member_id", member.getId()); // 强制注入当前用户ID进行过滤
        PageUtils page = memberReceiveAddressService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息 (增加水平越权校验)
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if (member == null) {
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);
        // 校验地址是否属于当前登录用户
        if (memberReceiveAddress != null && !memberReceiveAddress.getMemberId().equals(member.getId())) {
            throw new RRException(MSG_FORBIDDEN, CODE_FORBIDDEN);
        }
        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存 (强制设置 memberId)
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if(member == null){
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        memberReceiveAddress.setMemberId(member.getId()); // 强制覆盖前端传来的 ID
        memberReceiveAddressService.save(memberReceiveAddress);
        return R.ok();
    }
    /**
     * 修改 (增加水平越权校验)
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if (member == null) {
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        // 先查询旧记录，验证归属
        MemberReceiveAddressEntity oldAddress = memberReceiveAddressService.getById(memberReceiveAddress.getId());
        if (oldAddress == null || !oldAddress.getMemberId().equals(member.getId())) {
            throw new RRException(MSG_FORBIDDEN, CODE_FORBIDDEN);
        }
        
        memberReceiveAddress.setMemberId(member.getId()); // 再次加固防止篡改归属
        memberReceiveAddressService.updateById(memberReceiveAddress);
        return R.ok();
    }

    /**
     * 删除 (增加水平越权校验)
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();
        if (member == null) {
            throw new RRException(MSG_UNAUTHORIZED, CODE_UNAUTHORIZED);
        }
        if (ids == null || ids.length == 0) {
            throw new RRException("ids不能为空", 10001);
        }
        List<MemberReceiveAddressEntity> oldAddresses = memberReceiveAddressService.listByIds(Arrays.asList(ids));
        for (MemberReceiveAddressEntity oldAddress : oldAddresses) {
            if (oldAddress != null && !oldAddress.getMemberId().equals(member.getId())) {
                throw new RRException(MSG_FORBIDDEN, CODE_FORBIDDEN);
            }
        }
        memberReceiveAddressService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}
