package com.su.aspect;


import com.su.annotation.AutoFill;
import com.su.constant.AutoFillConstant;
import com.su.context.BaseContext;
import com.su.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.su.mapper.*.*(..)) " +
            "&& @annotation(com.su.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在方法执行前进行执行
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行数据填充");

        // 获取数据操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        LocalDateTime now= LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        Object object = args[0];
        OperationType value = autoFill.value();
        if(value == OperationType.INSERT){
            Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setCreateTime.invoke(object, now);
            setUpdateTime.invoke(object, now);
            setCreateUser.invoke(object, id);
            setUpdateUser.invoke(object, id);

        }else if (value == OperationType.UPDATE){
            Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(object, now);
            setUpdateUser.invoke(object, id);
        }



    }
}
