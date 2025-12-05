package com.pizza.libms.exception;

import com.pizza.libms.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
// 临时：将异常处理类以普通 Component 注册，避免被 springdoc 当作 ControllerAdvice 扫描。
// 目的是恢复 /v3/api-docs 可用性，后续会继续定位并修复根因，再恢复为 @RestControllerAdvice。
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null ?
                ex.getBindingResult().getFieldError().getDefaultMessage() : "参数校验失败";
        return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * 通用异常处理。
     * 临时调试逻辑：如果请求是访问 OpenAPI JSON（/v3/api-docs），返回异常堆栈的纯文本，便于调试 OpenAPI 生成问题。
     * 生产环境请在定位问题后移除或关闭此逻辑。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception ex) {
        return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常");
    }
}
