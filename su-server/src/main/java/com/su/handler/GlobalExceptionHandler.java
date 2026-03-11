package com.su.handler;

import com.su.constant.MessageConstant;
import com.su.exception.*;
import com.su.result.ErrorResponse;
import com.su.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理BadRequestException (400)
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.error("请求参数错误：{}", ex.getMessage(), ex);
        return new ErrorResponse(400, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理NotFoundException (404)
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        log.error("资源不存在：{}", ex.getMessage(), ex);
        return new ErrorResponse(404, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理ConflictException (409)
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException ex, HttpServletRequest request) {
        log.error("业务冲突：{}", ex.getMessage(), ex);
        return new ErrorResponse(409, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理UnauthorizedException (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        log.error("未授权：{}", ex.getMessage(), ex);
        return new ErrorResponse(401, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理ForbiddenException (403)
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        log.error("权限不足：{}", ex.getMessage(), ex);
        return new ErrorResponse(403, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理ServiceException (500)
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.error("服务器内部错误：{}", ex.getMessage(), ex);
        return new ErrorResponse(500, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理特定业务异常（保持向后兼容）
     */
    @ExceptionHandler({
        ExperiencePostNotFoundException.class,
        CommentNotFoundException.class,
        ProductNotFoundException.class,
        AccountNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundExceptions(BaseException ex, HttpServletRequest request) {
        log.error("资源不存在：{}", ex.getMessage(), ex);
        return new ErrorResponse(404, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
        DuplicateLikeException.class,
        DuplicateFollowException.class,
        SelfFollowException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictExceptions(BaseException ex, HttpServletRequest request) {
        log.error("业务冲突：{}", ex.getMessage(), ex);
        return new ErrorResponse(409, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
        ImageValidationException.class,
        ImageUploadException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleImageExceptions(BaseException ex, HttpServletRequest request) {
        log.error("图片处理错误：{}", ex.getMessage(), ex);
        return new ErrorResponse(400, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUserNotLoginException(UserNotLoginException ex, HttpServletRequest request) {
        log.error("用户未登录：{}", ex.getMessage(), ex);
        return new ErrorResponse(401, ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理其他BaseException（保持向后兼容）
     */
    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex){
        log.error("业务异常：{}", ex.getMessage(), ex);
        return Result.error(ex.getMessage());
    }

    /**
     * 处理数据库约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException ex, HttpServletRequest request) {
        log.error("数据库约束异常：{}", ex.getMessage(), ex);
        String message;
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            message = split[2] + MessageConstant.ALREADY_EXISTS;
        } else {
            message = "数据库操作失败";
        }
        return new ErrorResponse(400, message, request.getRequestURI());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("空指针异常", ex);
        return new ErrorResponse(500, "系统内部错误，请稍后重试", request.getRequestURI());
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("参数异常：{}", ex.getMessage(), ex);
        return new ErrorResponse(400, "参数错误：" + ex.getMessage(), request.getRequestURI());
    }

    /**
     * 处理未预期的异常 (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex, HttpServletRequest request) {
        log.error("系统异常", ex);
        return new ErrorResponse(500, MessageConstant.UNKNOWN_ERROR, request.getRequestURI());
    }

}
