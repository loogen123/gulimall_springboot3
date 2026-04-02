package com.lg.gulimail.thirdparty.service.impl;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.RRException;
import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = {
        "aliyun.sms.access-key-id",
        "aliyun.sms.access-key-secret",
        "aliyun.sms.sign-name",
        "aliyun.sms.template-code"
})
public class AliyunSmsServiceImpl implements AliyunSmsService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_SEND_PREFIX = "sms:send:";
    private static final String SMS_CHECK_FAIL_PREFIX = "sms:check:fail:";
    private static final long CODE_EXPIRE_MINUTES = 5L;
    private static final long SEND_INTERVAL_SECONDS = 60L;
    private static final int MAX_CHECK_FAIL_TIMES = 5;

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    private volatile com.aliyun.dypnsapi20170525.Client cachedClient;

    /**
     * 初始化 V2.0 客户端
     */
    private com.aliyun.dypnsapi20170525.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = "dypnsapi.aliyuncs.com";
        return new com.aliyun.dypnsapi20170525.Client(config);
    }

    private com.aliyun.dypnsapi20170525.Client getOrCreateClient() throws Exception {
        if (cachedClient != null) {
            return cachedClient;
        }
        synchronized (this) {
            if (cachedClient == null) {
                cachedClient = createClient();
            }
            return cachedClient;
        }
    }

    @Override
    public boolean sendVerifyCode(String phone) {
        try {
            String sendKey = SMS_SEND_PREFIX + phone;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sendKey))) {
                throw new RRException(BizCodeEnum.TOO_MANY_REQUESTS);
            }
            com.aliyun.dypnsapi20170525.Client client = getOrCreateClient();
            String code = generateVerifyCode();
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request =
                    new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                            .setSignName(this.signName)
                            .setTemplateCode(this.templateCode)
                            .setPhoneNumber(phone)
                            .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse resp =
                    client.sendSmsVerifyCodeWithOptions(request, runtime);

            if ("OK".equalsIgnoreCase(resp.body.code)) {
                redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set(sendKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
                return true;
            } else {
                log.warn("sms send failed: code={}, message={}", resp.body.code, resp.body.message);
                return false;
            }
        } catch (RRException e) {
            throw e;
        } catch (Exception e) {
            log.error("sms send exception", e);
            return false;
        }
    }

    @Override
    public boolean checkVerifyCode(String phone, String code) {
        if (phone == null || phone.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String failKey = SMS_CHECK_FAIL_PREFIX + phone;
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        int failCount = parseFailCount(failCountStr);
        if (failCount >= MAX_CHECK_FAIL_TIMES) {
            redisTemplate.delete(SMS_CODE_PREFIX + phone);
            return false;
        }
        String sCode = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        if (sCode != null && sCode.equals(code)) {
            redisTemplate.delete(SMS_CODE_PREFIX + phone);
            redisTemplate.delete(failKey);
            return true;
        }
        redisTemplate.opsForValue().increment(failKey);
        redisTemplate.expire(failKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return false;
    }

    private String generateVerifyCode() {
        int num = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(num);
    }

    private int parseFailCount(String failCountStr) {
        if (failCountStr == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(failCountStr));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
