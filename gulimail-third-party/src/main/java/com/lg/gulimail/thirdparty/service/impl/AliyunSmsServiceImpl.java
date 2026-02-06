package com.lg.gulimail.thirdparty.service.impl;

import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AliyunSmsServiceImpl implements AliyunSmsService {

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

    @Override
    public boolean sendVerifyCode(String phone) {
        try {
            com.aliyun.dypnsapi20170525.Client client = this.createClient();

            // 1. 生成验证码
            String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

            // 2. 构造请求（注意：短信认证服务的占位符通常是 ##code##）
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request =
                    new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                            .setSignName(this.signName)
                            .setTemplateCode(this.templateCode)
                            .setPhoneNumber(phone)
                            // 按照你提供的代码示例，这里填 ##code##，阿里云会自动替换或需要你手动拼接
                            .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

            // 3. 发送
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse resp =
                    client.sendSmsVerifyCodeWithOptions(request, runtime);

            // 4. 判断结果
            if ("OK".equalsIgnoreCase(resp.body.code)) {
                redisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
                return true;
            } else {
                System.out.println("发送失败: " + resp.body.message);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkVerifyCode(String phone, String code) {
        String sCode = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (sCode != null && sCode.equals(code)) {
            redisTemplate.delete("sms:code:" + phone);
            return true;
        }
        return false;
    }
}