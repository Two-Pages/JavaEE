import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ApiMonitorAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiMonitorAspect.class);

    private long totalExecutionTime = 0;
    private long totalRequests = 0;
    private long totalExceptions = 0;
    private long maxExecutionTime = Long.MIN_VALUE;
    private long minExecutionTime = Long.MAX_VALUE;

    @Pointcut("execution(* com.example.yourpackage.YourController.*(..))")
    public void monitorApiCalls() {}

    @Around("monitorApiCalls()")
    public Object aroundApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            totalExceptions++;
            throw throwable;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            totalExecutionTime += executionTime;
            totalRequests++;
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
            minExecutionTime = Math.min(minExecutionTime, executionTime);

            LOGGER.info("API: {} | Execution Time: {}ms", joinPoint.getSignature().toShortString(), executionTime);
        }
        return result;
    }

    @AfterThrowing(pointcut = "monitorApiCalls()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        LOGGER.error("Exception in API: {}", joinPoint.getSignature().toShortString());
        totalExceptions++;
    }

    @AfterReturning("monitorApiCalls()")
    public void logApiStats() {
        double averageExecutionTime = totalRequests > 0 ? (double) totalExecutionTime / totalRequests : 0;
        LOGGER.info("API Stats: Total Requests={}, Total Exceptions={}, " +
                        "Max Execution Time={}ms, Min Execution Time={}ms, Average Execution Time={}ms",
                totalRequests, totalExceptions, maxExecutionTime, minExecutionTime, averageExecutionTime);
    }
}
