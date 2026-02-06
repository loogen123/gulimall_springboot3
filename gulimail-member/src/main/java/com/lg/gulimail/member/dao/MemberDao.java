package com.lg.gulimail.member.dao;

import com.lg.gulimail.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 18:11:41
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
