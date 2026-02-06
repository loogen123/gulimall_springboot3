package com.lg.common.vo;

import java.io.Serializable;

public class UserLoginVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String loginacct;
    private String password;

    // 手动右键 Generate -> Getter and Setter
    public String getLoginacct() { return loginacct; }
    public void setLoginacct(String loginacct) { this.loginacct = loginacct; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}