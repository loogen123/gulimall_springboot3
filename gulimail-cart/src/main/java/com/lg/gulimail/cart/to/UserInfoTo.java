package com.lg.gulimail.cart.to;

import lombok.Data;

@Data
public class UserInfoTo {
    private Long userId;        // 登录后的用户ID
    private String userKey;     // 游客身份的UUID
    private Boolean tempUser = false; // 是否是临时用户
}