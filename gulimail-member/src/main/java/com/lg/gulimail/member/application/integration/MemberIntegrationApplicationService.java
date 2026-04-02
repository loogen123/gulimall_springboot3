package com.lg.gulimail.member.application.integration;

import com.lg.common.exception.BizCodeEnum;
import com.lg.gulimail.member.application.port.out.MemberIntegrationPort;
import com.lg.gulimail.member.domain.integration.MemberIntegrationCommand;
import com.lg.gulimail.member.domain.integration.MemberIntegrationDomainService;
import com.lg.gulimail.member.domain.integration.MemberIntegrationMutationResult;
import com.lg.gulimail.member.domain.integration.MemberIntegrationQuoteResult;
import com.lg.gulimail.member.entity.IntegrationChangeHistoryEntity;
import com.lg.gulimail.member.entity.MemberEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class MemberIntegrationApplicationService {
    private final MemberIntegrationPort memberIntegrationPort;
    private final MemberIntegrationDomainService memberIntegrationDomainService;

    public MemberIntegrationApplicationService(MemberIntegrationPort memberIntegrationPort, MemberIntegrationDomainService memberIntegrationDomainService) {
        this.memberIntegrationPort = memberIntegrationPort;
        this.memberIntegrationDomainService = memberIntegrationDomainService;
    }

    public MemberIntegrationQuoteResult quote(Map<String, Object> request) {
        MemberIntegrationCommand command = memberIntegrationDomainService.normalize(fromRequest(request));
        MemberIntegrationQuoteResult validateResult = memberIntegrationDomainService.validateQuoteCommand(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        MemberEntity member = memberIntegrationPort.findMemberById(command.getMemberId());
        if (member == null) {
            return MemberIntegrationQuoteResult.failure(BizCodeEnum.NOT_FOUND_EXCEPTION.getCode(), "会员不存在");
        }
        return memberIntegrationDomainService.resolveQuote(member.getIntegration(), command.getUseIntegration(), command.getOrderTotal());
    }

    public MemberIntegrationMutationResult deduct(Map<String, Object> request) {
        MemberIntegrationCommand command = memberIntegrationDomainService.normalize(fromRequest(request));
        MemberIntegrationMutationResult validateResult = memberIntegrationDomainService.validateMutationCommand(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        String deductNote = memberIntegrationDomainService.deductNote(command.getOrderSn());
        IntegrationChangeHistoryEntity existed = memberIntegrationPort.findHistoryByMemberIdAndNote(command.getMemberId(), deductNote);
        if (existed != null) {
            return MemberIntegrationMutationResult.success();
        }
        MemberEntity member = memberIntegrationPort.findMemberById(command.getMemberId());
        if (member == null) {
            return MemberIntegrationMutationResult.failure(BizCodeEnum.NOT_FOUND_EXCEPTION.getCode(), "会员不存在");
        }
        int available = member.getIntegration() == null ? 0 : Math.max(member.getIntegration(), 0);
        int realUse = Math.min(command.getUseIntegration(), available);
        if (realUse <= 0) {
            return MemberIntegrationMutationResult.success();
        }
        member.setIntegration(available - realUse);
        memberIntegrationPort.updateMember(member);
        IntegrationChangeHistoryEntity history = new IntegrationChangeHistoryEntity();
        history.setMemberId(command.getMemberId());
        history.setChangeCount(-realUse);
        history.setNote(deductNote);
        history.setSourceTyoe(0);
        history.setCreateTime(new Date());
        memberIntegrationPort.saveHistory(history);
        return MemberIntegrationMutationResult.success();
    }

    public MemberIntegrationMutationResult revert(Map<String, Object> request) {
        MemberIntegrationCommand command = memberIntegrationDomainService.normalize(fromRequest(request));
        MemberIntegrationMutationResult validateResult = memberIntegrationDomainService.validateMutationCommand(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        String revertNote = memberIntegrationDomainService.revertNote(command.getOrderSn());
        IntegrationChangeHistoryEntity reverted = memberIntegrationPort.findHistoryByMemberIdAndNote(command.getMemberId(), revertNote);
        if (reverted != null) {
            return MemberIntegrationMutationResult.success();
        }
        String deductNote = memberIntegrationDomainService.deductNote(command.getOrderSn());
        IntegrationChangeHistoryEntity deducted = memberIntegrationPort.findHistoryByMemberIdAndNote(command.getMemberId(), deductNote);
        if (deducted == null) {
            return MemberIntegrationMutationResult.success();
        }
        MemberEntity member = memberIntegrationPort.findMemberById(command.getMemberId());
        if (member == null) {
            return MemberIntegrationMutationResult.failure(BizCodeEnum.NOT_FOUND_EXCEPTION.getCode(), "会员不存在");
        }
        int current = member.getIntegration() == null ? 0 : member.getIntegration();
        member.setIntegration(current + command.getUseIntegration());
        memberIntegrationPort.updateMember(member);
        IntegrationChangeHistoryEntity history = new IntegrationChangeHistoryEntity();
        history.setMemberId(command.getMemberId());
        history.setChangeCount(command.getUseIntegration());
        history.setNote(revertNote);
        history.setSourceTyoe(0);
        history.setCreateTime(new Date());
        memberIntegrationPort.saveHistory(history);
        return MemberIntegrationMutationResult.success();
    }

    private MemberIntegrationCommand fromRequest(Map<String, Object> request) {
        MemberIntegrationCommand command = new MemberIntegrationCommand();
        command.setMemberId(parseLong(request == null ? null : request.get("memberId")));
        command.setUseIntegration(parseInteger(request == null ? null : request.get("useIntegration")));
        command.setOrderTotal(parseBigDecimal(request == null ? null : request.get("orderTotal")));
        command.setOrderSn(request == null ? null : String.valueOf(request.get("orderSn")));
        return command;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
