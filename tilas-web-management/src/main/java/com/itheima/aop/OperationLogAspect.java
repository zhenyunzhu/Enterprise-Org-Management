package com.itheima.aop;

import com.itheima.anno.Log;
import com.itheima.mapper.OperateLogMapper;
import com.itheima.pojo.OperateLog;
import com.itheima.util.CurrentHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面。
 *
 * <p>拦截所有标注 {@link Log} 的方法，记录操作人、时间、类名、方法名、入参、
 * 返回值与执行耗时。日志写入采用「环绕通知 + finally 块」，保证目标方法抛出
 * 异常时同样留痕；同时日志写入失败被完全吞掉，绝不影响主业务流程。</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    /** operate_log.method_params 为 varchar(1000)，超长必须截断，否则入库报错 */
    private static final int MAX_PARAMS_LENGTH = 1000;

    /** operate_log.return_value 为 varchar(2000) */
    private static final int MAX_RETURN_VALUE_LENGTH = 2000;

    private final OperateLogMapper operateLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            saveLog(joinPoint, result, error, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 落库操作日志。任何异常都被捕获并降级为告警日志，避免影响业务结果。
     */
    private void saveLog(ProceedingJoinPoint joinPoint, Object result, Throwable error, long costTime) {
        try {
            OperateLog operateLog = new OperateLog();
            operateLog.setOperateEmpId(CurrentHolder.getCurrentId());
            operateLog.setOperateTime(LocalDateTime.now());
            operateLog.setClassName(joinPoint.getTarget().getClass().getName());
            operateLog.setMethodName(joinPoint.getSignature().getName());
            operateLog.setMethodParams(truncate(toJson(joinPoint.getArgs()), MAX_PARAMS_LENGTH));
            operateLog.setReturnValue(truncate(
                    error == null ? toJson(result) : "EXCEPTION: " + error.getMessage(),
                    MAX_RETURN_VALUE_LENGTH));
            operateLog.setCostTime(costTime);
            operateLogMapper.insert(operateLog);
        } catch (Exception e) {
            log.warn("操作日志写入失败, method={}, 原因={}",
                    joinPoint.getSignature().getName(), e.getMessage());
        }
    }

    /**
     * 序列化为 JSON 文本；序列化失败时退化为 toString，保证切面不抛异常。
     */
    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value instanceof Object[] array ? Arrays.toString(array) : String.valueOf(value);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

}
