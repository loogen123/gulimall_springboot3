package com.lg.common.exception;

import com.lg.common.utils.R;
import com.lg.common.utils.RRException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice // 不写 basePackages，全路径生效
public class GulimailExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();

        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        // 使用枚举类定义的错误码 10001
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg())
                .put("data", errorMap);
    }

    @ExceptionHandler(value = RRException.class)
    public R handleRRException(RRException e) {
        log.warn("业务异常：{}", e.getMsg());
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable t) {
        log.error("错误：", t);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
