package com.lg.gulimail.member.application.port.out;

import com.lg.gulimail.member.entity.IntegrationChangeHistoryEntity;
import com.lg.gulimail.member.entity.MemberEntity;

public interface MemberIntegrationPort {
    MemberEntity findMemberById(Long memberId);

    void updateMember(MemberEntity memberEntity);

    IntegrationChangeHistoryEntity findHistoryByMemberIdAndNote(Long memberId, String note);

    void saveHistory(IntegrationChangeHistoryEntity historyEntity);
}
