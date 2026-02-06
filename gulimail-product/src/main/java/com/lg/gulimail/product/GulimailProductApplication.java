package com.lg.gulimail.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//整合mybatis-plus
//1.导入依赖
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.lg.gulimail.product.dao")
@EnableFeignClients(basePackages = "com.lg.gulimail.product.feign")
@EnableCaching
//逻辑删除
//3.JSR303
//1)给bean添加校验注解：javax.validation.constraints，并定义自己的message提示
//2）开启校验功能@Validated
//3)给校验的bean紧跟一个bindingResult，就可以获得校验的结果
//4）分组校验
//      1）@NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
//          给校验注解标注什么时候要进行校验
//      2）@Validated({AddGroup.class})标注controller方法使用哪个分组的校验
//      3）默认没有指定分组到底校验注解比如@NotBlank,在分组校验的情况@Vaildated({AddGroup.class})下不生效
//      4)自定义校验
//          1）编写一个自定义的校验注解
//          2）编写一个自定义的校验器
//          3）关联自定义的校验器和校验注解
//4.统一的异常处理
//1)编写异常处理类，使用@ControllerAdvice
//2）使用@ExceptionHandler进行异常处理，返回json数据



//模板引擎：thymeleaf
//静态资源都放在static目录下，thymeleaf默认就会去static目录下寻找页面
//页面放在templates下，thymeleaf默认就会去templates下寻找index.html
public class GulimailProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailProductApplication.class, args);
    }

}
