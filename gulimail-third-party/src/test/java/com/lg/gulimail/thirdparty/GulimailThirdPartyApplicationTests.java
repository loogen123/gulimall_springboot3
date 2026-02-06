package com.lg.gulimail.thirdparty;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
class GulimailThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;

    @Test
    public void testUpload() throws Exception {
        // 1. 准备文件流
        // 建议找一个路径简单的文件，或者直接放在 resources 下
        String bucketName = "asdasd-wqe";
        String objectName = "test-picture.png"; // 云端保存的文件名
        String localPath = "C:/Users/13808/Pictures/Screenshots/picture1.png";

        InputStream inputStream = new FileInputStream(localPath);

        // 2. 执行上传
        System.out.println("开始上传...");
        ossClient.putObject(bucketName, objectName, inputStream);
        System.out.println("上传完成！");

        // 3. 关闭流（OSSClient 不要手动 shutdown，否则后续测试会挂）
        inputStream.close();
    }
}
