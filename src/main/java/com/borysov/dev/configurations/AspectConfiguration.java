package com.borysov.dev.configurations;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class AspectConfiguration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.borysov.dev.services..*.*(..))")
    public void logServiceMethodName(JoinPoint joinPoint) {
        logger.info(joinPoint.getSignature().getName());
    }
}