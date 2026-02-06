package com.lg.common.vo;

import lombok.Data;

@Data
public class SocialUser {
    private String accessToken;
    private String tokenType;
    private String scope;
    
    // GitHub 的唯一标识 ID (注意：GitHub 返回的是数字或字符串，建议用 String 接收)
    // 如果你通过 token 换取用户信息，GitHub 会返回一个名为 "id" 的字段
    private String uid; 
}