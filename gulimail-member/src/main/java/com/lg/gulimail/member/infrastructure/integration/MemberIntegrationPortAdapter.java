package com.lg.gulimail.member.infrastructure.integration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lg.gulimail.member.application.port.out.MemberIntegrationPort;
import com.lg.gulimail.member.entity.IntegrationChangeHistoryEntity;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.service.IntegrationChangeHistoryService;
import com.lg.gulimail.member.service.MemberService;
import org.springframework.stereotype.Component;

@Component
public class MemberIntegrationPortAdapter implements MemberIntegrationPort {
    private final MemberService memberService;
    private final IntegrationChangeHistoryService integrationChangeHistoryService;

    public MemberIntegrationPortAdapter(MemberService memberService, IntegrationChangeHistoryService integrationChangeHistoryService) {
        this.memberService = memberService;
        this.integrationChangeHistoryService = integrationChangeHistoryService;
    }

    @Override
    public MemberEntity findMemberById(Long memberId) {
        return memberService.getById(memberId);
    }

    @Override
    public void updateMember(MemberEntity memberEntity) {
        memberService.updateById(memberEntity);
    }

    @Override
    public IntegrationChangeHistoryEntity findHistoryByMemberIdAndNote(Long memberId, String note) {
        return integrationChangeHistoryService.getOne(
                new QueryWrapper<IntegrationChangeHistoryEntity>()
                        .eq("member_id", memberId)
                        .eq("note", note)
                        .last("limit 1")
        );
    }

    @Override
    public void saveHistory(IntegrationChangeHistoryEntity historyEntity) {
        integrationChangeHistoryService.save(historyEntity);
    }
}
