package com.lg.gulimail.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lg.gulimail.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @ConfigurationProperties(prefix = "alipay")
 * 会自动将 Nacos 中以 alipay 开头的配置（如 alipay.appId）赋值给类中同名的属性
 */
@ConfigurationProperties(prefix = "alipay")
@RefreshScope
@Component
@Data
public class AlipayTemplate {

    // 以下属性将从 Nacos 的 gulimail-order.yml 中读取
    private String appId;
    private String merchantPrivateKey;
    private String alipayPublicKey;

    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String returnUrl = "http://order.gulimail.com/list.html";
    private String notifyUrl = "http://c536a58d.natappfree.cc/order/payed/notify";

    // 签名算法类型，固定为 RSA2
    private String signType = "RSA2";
    // 字符编码
    private String charset = "utf-8";
    // 格式
    private String format = "json";

    public String pay(PayVo vo) throws AlipayApiException {
        // 1. 根据从 Nacos 读取到的参数创建客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                gatewayUrl,
                appId,
                merchantPrivateKey,
                format,
                charset,
                alipayPublicKey,
                signType);

        // 2. 设置请求参数（电脑网站支付）
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        // 3. 封装业务参数
        // 注意：字段名必须严格遵守支付宝 API 文档
        request.setBizContent("{\"out_trade_no\":\"" + vo.getOut_trade_no() + "\","
                + "\"total_amount\":\"" + vo.getTotal_amount() + "\","
                + "\"subject\":\"" + vo.getSubject() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        // 4. 生成表单并返回给前端渲染
        return alipayClient.pageExecute(request).getBody();
    }
}