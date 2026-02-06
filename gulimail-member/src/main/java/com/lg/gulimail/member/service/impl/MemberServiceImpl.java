package com.lg.gulimail.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.member.dao.MemberDao;
import com.lg.gulimail.member.dao.MemberLevelDao;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.entity.MemberLevelEntity;
import com.lg.gulimail.member.exception.PhoneExistException;
import com.lg.gulimail.member.exception.UsernameExistException;
import com.lg.gulimail.member.service.MemberService;
import com.lg.gulimail.member.vo.MemberLoginVo;
import com.lg.gulimail.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MemberLevelDao memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity entity = new MemberEntity();

        // 1. 获取默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();

        if (levelEntity != null) {
            entity.setLevelId(levelEntity.getId());
        } else {
            // --- 核心改进：如果数据库没数据，代码自动初始化一个 ---
            MemberLevelEntity defaultLevel = new MemberLevelEntity();
            defaultLevel.setName("普通会员");
            defaultLevel.setDefaultStatus(1);
            defaultLevel.setGrowthPoint(0);
            // 先把这个等级存入数据库
            memberLevelDao.insert(defaultLevel);
            // 拿到自动生成的 ID 给新用户
            entity.setLevelId(defaultLevel.getId());
        }

        // 2. 检查唯一性
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        // 3. 填充其他信息并加密密码...
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        entity.setPassword(passwordEncoder.encode(vo.getPassword()));

        this.baseMapper.insert(entity);
    }
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        // 1. 在数据库中查询是否有该手机号的记录
        Long count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            // 2. 如果有，抛出自定义异常
            throw new PhoneExistException();
        }
    }

    public void checkUserNameUnique(String userName) throws UsernameExistException {
        // 1. 在数据库中查询是否有该用户名的记录
        Long count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            // 2. 如果有，抛出自定义异常
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct(); // 可能是用户名、手机号或邮箱
        String password = vo.getPassword();   // 用户输入的明文密码

        // 1. 去数据库查询该用户
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or().eq("mobile", loginacct));

        if (entity == null) {
            // 登录失败：用户名不存在
            return null;
        } else {
            // 2. 获取数据库里的密文
            String passwordDb = entity.getPassword();
            // 3. 使用 BCrypt 进行密文匹配 (关键！)
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);

            if (matches) {
                // 登录成功，返回用户信息
                return entity;
            } else {
                // 登录失败：密码错误
                return null;
            }
        }
    }
    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        // 1. 打印 Token 详情，检查是否包含 "access_token=" 等多余前缀
        System.out.println("======= GitHub 登录调试信息 =======");
        System.out.println("收到 AccessToken: " + socialUser.getAccessToken());
        System.out.println("==================================");

        String url = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();

        // 统一使用 Bearer 方式，这是 GitHub 最标准的认证头
        headers.set("Authorization", "Bearer " + socialUser.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String json = response.getBody();
                System.out.println("GitHub 用户信息获取成功: " + json);
                JSONObject jsonObject = JSON.parseObject(json);

                String uid = jsonObject.getString("id");
                String nickname = jsonObject.getString("login");
                String headerImg = jsonObject.getString("avatar_url");

                // 2. 数据库逻辑
                MemberEntity memberEntity = this.baseMapper.selectOne(
                        new QueryWrapper<MemberEntity>().eq("social_uid", uid));

                if (memberEntity != null) {
                    // 老用户更新
                    MemberEntity update = new MemberEntity();
                    update.setId(memberEntity.getId());
                    update.setAccessToken(socialUser.getAccessToken());
                    this.baseMapper.updateById(update);
                    memberEntity.setAccessToken(socialUser.getAccessToken());
                    return memberEntity;
                } else {
                    // 新用户注册
                    MemberEntity register = new MemberEntity();
                    register.setNickname(nickname);
                    register.setHeader(headerImg);
                    register.setSocialUid(uid);
                    register.setAccessToken(socialUser.getAccessToken());
                    register.setLevelId(1L); // 默认等级
                    register.setCreateTime(new Date());
                    this.baseMapper.insert(register);
                    return register;
                }
            }
        } catch (Exception e) {
            System.err.println("远程获取 GitHub 用户信息失败！错误详情：");
            e.printStackTrace();
        }
        return null;
    }
}