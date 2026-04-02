package com.lg.common.vo;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class UserLoginVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String loginacct;
    @NotBlank(message = "密码不能为空")
    private String password;

    // 手动右键 Generate -> Getter and Setter
    public String getLoginacct() { return loginacct; }
    public void setLoginacct(String loginacct) { this.loginacct = loginacct; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
