package com.mitchtalmadge.uofu_cs_bot;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GlobalExceptionHandler {

    private final LogService logService;

    @Autowired
    public GlobalExceptionHandler(LogService logService) {
        this.logService = logService;
    }

    @AfterThrowing(pointcut = "execution(* com.mitchtalmadge.uofu_cs_bot..*.*(..))", throwing = "ex")
    public void handleUncaughtException(JoinPoint joinPoint, Throwable ex) {
        logService.logException(joinPoint.getSourceLocation().getWithinType(), ex, "Uncaught Exception");
    }

}
