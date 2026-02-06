package com.lg.common.vo;

import lombok.Data;

@Data
public class MemberAddressVo {
    private Long id;
    private Long memberId;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
    private Integer defaultStatus;
}