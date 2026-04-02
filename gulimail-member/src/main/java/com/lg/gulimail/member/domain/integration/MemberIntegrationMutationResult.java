package com.lg.gulimail.member.domain.integration;

import lombok.Data;

@Data
public class MemberIntegrationMutationResult {
    private Integer code;
    private String message;

    public static MemberIntegrationMutationResult success() {
        MemberIntegrationMutationResult result = new MemberIntegrationMutationResult();
        result.setCode(0);
        result.setMessage("success");
        return result;
    }

    public static MemberIntegrationMutationResult failure(Integer code, String message) {
        MemberIntegrationMutationResult result = new MemberIntegrationMutationResult();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
