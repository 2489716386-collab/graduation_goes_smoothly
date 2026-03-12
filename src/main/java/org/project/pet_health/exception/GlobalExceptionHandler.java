package org.project.pet_health.exception;

import lombok.extern.slf4j.Slf4j;
import org.project.pet_health.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /*
    运行时异常处理
     */

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result<?> exception() {
        return Result.error();
    }

    @ExceptionHandler(value = UserException.class)
    @ResponseBody
    public Result<?> UserExceptionHandler(final UserException e) {
        log.error("错误原因为:"+e.getMessage());
        return Result.error(e.getMessage());
    }

    /*
    对入参进行校验
     */

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<?> MethodArgumentNotValidExceptionHandler(final MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String defaultMessage = fieldError.getDefaultMessage();
        log.error("错误原因为："+defaultMessage);
        return Result.error(defaultMessage);
    }
}
