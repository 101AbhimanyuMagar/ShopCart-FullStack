package com.shopcart.shopcart_backend.aop;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @PostConstruct
    public void init() {
        log.info("🔥 LoggingAspect initialized");
    }

    @Around("execution(* com.shopcart.shopcart_backend.controllers.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();

        log.info("➡️ Entering API: {}", methodName);

        try {

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;

            log.info("✅ Completed API: {} | Time Taken: {} ms",
                    methodName,
                    executionTime);

            return result;

        } catch (Exception ex) {

            log.error("❌ Exception in API: {} | Message: {}",
                    methodName,
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }
}