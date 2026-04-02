package com.lg.gulimail.authserver.application.auth;

import com.alibaba.fastjson.TypeReference;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.application.port.out.AuthMemberPort;
import com.lg.gulimail.authserver.application.port.out.AuthThirdPartyPort;
import com.lg.gulimail.authserver.domain.auth.AuthDomainService;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginApplicationService {
    private static final String GITHUB_OAUTH_STATE = "github_oauth_state";
    private final AuthMemberPort authMemberPort;
    private final AuthThirdPartyPort authThirdPartyPort;
    private final AuthDomainService authDomainService;

    public LoginApplicationService(AuthMemberPort authMemberPort, AuthThirdPartyPort authThirdPartyPort, AuthDomainService authDomainService) {
        this.authMemberPort = authMemberPort;
        this.authThirdPartyPort = authThirdPartyPort;
        this.authDomainService = authDomainService;
    }

    public String prepareGithubAuthUrl(HttpSession session, String authorizeUrl, String clientId, String redirectUri) {
        String state = authDomainService.generateState();
        session.setAttribute(GITHUB_OAUTH_STATE, state);
        return authorizeUrl + "?client_id=" + clientId + "&redirect_uri=" + redirectUri + "&state=" + state;
    }

    public R sendCode(String phone) {
        String normalizedPhone = authDomainService.normalizePhone(phone);
        if (!authDomainService.isValidPhone(normalizedPhone)) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "手机号格式错误");
        }
        return authThirdPartyPort.sendCode(normalizedPhone);
    }

    public R register(UserRegisterVo vo, BindingResult bindingResult) {
        Map<String, String> errors = authDomainService.toErrorMap(bindingResult);
        if (!errors.isEmpty()) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "数据校验失败").put("errors", errors);
        }
        R checkCodeResult = authThirdPartyPort.checkCode(vo.getPhone(), vo.getCode());
        if (checkCodeResult.getCode() != 0) {
            Map<String, String> codeErrors = new HashMap<>();
            codeErrors.put("code", "验证码错误或已过期");
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "验证码校验失败").put("errors", codeErrors);
        }
        R registerResult = authMemberPort.register(vo);
        if (registerResult.getCode() == 0) {
            return R.ok();
        }
        String msg = (String) registerResult.get("msg");
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), StringUtils.hasText(msg) ? msg : "注册失败，请稍后再试");
    }

    public R login(UserLoginVo vo, BindingResult bindingResult, HttpSession session) {
        Map<String, String> errors = authDomainService.toErrorMap(bindingResult);
        if (!errors.isEmpty()) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "数据校验失败").put("errors", errors);
        }
        R loginResult = authMemberPort.login(vo);
        if (loginResult.getCode() == 0) {
            MemberResponseVo member = loginResult.getData("member", new TypeReference<MemberResponseVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, member);
            return R.ok();
        }
        String msg = (String) loginResult.get("msg");
        return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                StringUtils.hasText(msg) ? msg : BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
    }

    public void logout(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(AuthServerConstant.LOGIN_USER);
        session.invalidate();
    }
}
