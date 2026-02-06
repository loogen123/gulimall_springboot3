package com.lg.common.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
//同一个校验注解可以指定多个不同的校验器进行校验
@Constraint(validatedBy = {ListValueConstraintValidator.class}) // 指定校验器
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    // 默认错误消息，去配置文件或这里取
    String message() default "{com.lg.common.valid.ListValue.message}";

    // 分组校验
    Class<?>[] groups() default {};

    // 负载信息
    Class<? extends Payload>[] payload() default {};

    // 自定义属性：定义允许出现的数值数组
    int[] vals() default {};
}