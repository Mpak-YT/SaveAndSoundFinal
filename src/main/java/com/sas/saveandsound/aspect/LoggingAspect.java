package com.sas.saveandsound.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Логирование вызовов методов контроллеров
    @Before("execution(* com.sas.saveandsound.controller..*(..))")
    public void logControllerMethodInvocation(JoinPoint joinPoint) {
        logger.info("Controller method invoked: {}", joinPoint.getSignature());
    }

    // Логирование исключений, выбрасываемых в сервисах
    @AfterThrowing(pointcut = "execution(* com.sas.saveandsound.service..*(..))", throwing = "exception")
    public void logServiceExceptions(JoinPoint joinPoint, Throwable exception) {
        logger.error("Exception thrown in method: {}. Message: {}",
                joinPoint.getSignature(), exception.getMessage());
    }
}
