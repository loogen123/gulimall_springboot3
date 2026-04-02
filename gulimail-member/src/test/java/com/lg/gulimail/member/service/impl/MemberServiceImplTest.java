package com.lg.gulimail.member.service.impl;

import com.lg.gulimail.member.dao.MemberDao;
import com.lg.gulimail.member.dao.MemberLevelDao;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.vo.MemberLoginVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberDao memberDao;

    @Mock
    private MemberLevelDao memberLevelDao;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void loginShouldReturnMemberWhenPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(memberService, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(memberService, "baseMapper", memberDao);
        MemberEntity member = new MemberEntity();
        member.setId(1L);
        member.setPassword(encoder.encode("123456"));
        when(memberDao.selectOne(any())).thenReturn(member);
        MemberLoginVo loginVo = new MemberLoginVo();
        loginVo.setLoginacct("u1");
        loginVo.setPassword("123456");

        MemberEntity result = memberService.login(loginVo);

        assertNotNull(result);
    }

    @Test
    void loginShouldReturnNullWhenPasswordNotMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(memberService, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(memberService, "baseMapper", memberDao);
        MemberEntity member = new MemberEntity();
        member.setId(1L);
        member.setPassword(encoder.encode("123456"));
        when(memberDao.selectOne(any())).thenReturn(member);
        MemberLoginVo loginVo = new MemberLoginVo();
        loginVo.setLoginacct("u1");
        loginVo.setPassword("bad");

        MemberEntity result = memberService.login(loginVo);

        assertNull(result);
    }
}
