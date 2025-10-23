package com.example.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

/**
 * Cross-cutting logging for controllers, services, and repositories.
 */
@Aspect
@Configuration
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.user_service..controller..*(..)) || " +
            "execution(* com.example.user_service..service..*(..)) || " +
            "execution(* com.example.user_service..repo..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        log.info("ENTER {} args={}", signature, joinPoint.getArgs());
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("EXIT  {} result={} in {} ms", signature, result, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("ERROR {} after {} ms", signature, duration, ex);
            throw ex;
        }
    }
}

