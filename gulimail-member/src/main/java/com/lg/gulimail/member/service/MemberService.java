package com.lg.gulimail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.utils.PageUtils;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.vo.MemberLoginVo;
import com.lg.gulimail.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 18:11:41
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

