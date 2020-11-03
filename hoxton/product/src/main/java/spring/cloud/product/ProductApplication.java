package spring.cloud.product;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerMetricsAutoConfiguration;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import io.github.resilience4j.ratelimiter.autoconfigure.RateLimiterMetricsAutoConfiguration;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@SpringBootApplication(scanBasePackages = "spring.cloud.product",
        // 排除Resilience4j Spring Boot stater的自动装配类
        
        exclude = {
                CircuitBreakerAutoConfiguration.class,
                CircuitBreakerMetricsAutoConfiguration.class,
                RateLimiterAutoConfiguration.class,
                RateLimiterMetricsAutoConfiguration.class
        }

)

// 在新版本的 Spring Cloud中 ，不再需要这个注解驱动服务发现了
//@EnableDiscoveryClient
// 驱动断路器
@EnableCircuitBreaker
public class ProductApplication {

    // 负载均衡
    @LoadBalanced
    // 创建Spring Bean
    @Bean
    public RestTemplate initRestTemplate() {
        return new RestTemplate();
    }

    // 断路器配置
    private CircuitBreakerConfig circuitBreakerConfig = null;
    // 断路器注册机
    private CircuitBreakerRegistry circuitBreakerRegistry = null;

    @Bean(name = "circuitBreakerConfig")
    public CircuitBreakerConfig initCircuitBreakerConfig() {
        if (circuitBreakerConfig == null) {
//            circuitBreakerConfig = CircuitBreakerConfig.custom().// 自定义配置
//                    // 当请求失败比例达到30%时，打开断路器，默认为50%
//                            failureRateThreshold(30)
//                    // 当断路器为打开状态时，等待多少时间，转变为半打开状态，默认为60秒
//                    .waitDurationInOpenState(Duration.ofSeconds(5))
//                    // 配置断路器半打开时的环形位缓冲区大小（假设记为n，默认为10），
//                    // 在等待n次请求后，才重新分析请求结果来确定是否改变断路器的状态
//                    .ringBufferSizeInHalfOpenState(5)
//                    // 配置断路器闭合时环形位缓冲区的大小（假设记为n，默认为100），
//                    // 在等待n次请求后，才重新分析请求结果来确定是否改变断路器的状态
//                    .ringBufferSizeInClosedState(5)
//                    // 构建建立配置
//                    .build();

            circuitBreakerConfig = CircuitBreakerConfig.custom().// 自定义配置
                    // 当请求失败比例达到30%时，打开断路器，默认为50%
                            failureRateThreshold(30)
                    // 当断路器为打开状态时，等待多少时间，转变为半打开状态，默认为60秒
                    .waitDurationInOpenState(Duration.ofSeconds(5))
                    // 配置断路器半打开时的环形缓冲区大小（假设记为n，默认为10），
                    // 在等待n次请求后，才重新分析请求结果来确定是否改变断路器的状态
                    .ringBufferSizeInHalfOpenState(5)
                    // 配置断路器闭合时环形缓冲区的大小（假设记为n，默认为100），
                    // 在等待n次请求后，才重新分析请求结果来确定是否改变断路器的状态
                    .ringBufferSizeInClosedState(5)
                    // 断路器异常处理
                    .recordFailure(ex -> {
                        System.out.println("发生了异常，栈追踪信息为： ");
                        ex.printStackTrace();
                        return false;
                    }) // 忽略哪些异常，即当发生这些异常时，不认为执行失败
                    .ignoreExceptions(ClassNotFoundException.class, IOException.class)
                    // 只有在发生哪些异常时，才认为执行失败
                    .recordExceptions(Exception.class, RuntimeException.class)
                    // 构建建立配置
                    .build();
        }
        return circuitBreakerConfig;
    }

    // 构建断路器注册机
    @Bean(name = "circuitBreakerRegistry")
    public CircuitBreakerRegistry initCircuitBreakerRegistry() {
        if (circuitBreakerConfig == null) {
            initCircuitBreakerConfig();
        }
        if (circuitBreakerRegistry == null) {
            // 创建断路器注册机
            circuitBreakerRegistry =
                    CircuitBreakerRegistry.of(circuitBreakerConfig);
        }
        return circuitBreakerRegistry;
    }


    // 限速器配置
    private RateLimiterConfig rateLimiterConfig = null;
    // 限速器注册机
    private RateLimiterRegistry rateLimiterRegistry = null;

    // 初始化限速器配置
    @Bean(name = "rateLimiterConfig")
    public RateLimiterConfig initRateLimiterConfig() {
        if (rateLimiterConfig == null) {
            // 定义限制20req/s的限流器
            rateLimiterConfig = RateLimiterConfig.custom()
                    // 采用自定义
                    // 配置时间戳，默认值为500 ns
                    .limitRefreshPeriod(Duration.ofSeconds(1))
                    // 时间戳内限制通过的请求数，默认值为50
                    .limitForPeriod(20)
                    // 配置超时，如果等待超时则限速器丢弃请求，默认值为5秒
                    .timeoutDuration(Duration.ofSeconds(2)).build();
        }
        return rateLimiterConfig;
    }

    // 初始化限速器注册机
    @Bean(name = "rateLimiterRegistry")
    public RateLimiterRegistry initRateLimiterRegistry() {
        if (rateLimiterConfig == null) {
            initRateLimiterConfig();
        }
        if (rateLimiterRegistry == null) {
            // 设置默认的限速配置，创建限速器注册机
            rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);
            // 创建断路器，并注册在注册机内（使用默认配置）
            rateLimiterRegistry.rateLimiter("user");
        }
        return rateLimiterRegistry;
    }


    // 舱壁注册机
    private BulkheadRegistry bulkheadRegistry;
    // 舱壁配置
    private BulkheadConfig bulkheadConfig;

    @Bean(name = "bulkheadConfig")
    public BulkheadConfig initBulkheadConfig() {
        if (bulkheadConfig == null) {
            // 舱壁配置
            bulkheadConfig = BulkheadConfig.custom()
                    // 最大并发数，默认值为25
                    .maxConcurrentCalls(20)
                    /* 调度线程最大等待时间（单位毫秒），默认值为0，
                     如果存在高并发场景，强烈建议设置为0，
                     如果不设置为0，那么在高并发场景下，
                     可能会导致线程积压，引发各类问题*/
                    .maxWaitTime(0)
                    .build();
        }
        return bulkheadConfig;
    }

    @Bean(name = "bulkheadRegistry")
    public BulkheadRegistry initBulkheadRegistry() {
        if (bulkheadConfig == null) {
            initBulkheadConfig();
        }
        if (bulkheadRegistry == null) {
            // 创建舱壁注册机，并设置默认配置
            bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
            // 创建一个命名为user的舱壁
            bulkheadRegistry.bulkhead("user");
        }
        return bulkheadRegistry;
    }

    //重试器（Retry）
    private RetryConfig retryConfig = null;
    private RetryRegistry retryRegistry = null;

    @Bean(name = "retryConfig")
    public RetryConfig initRetryConfig() {
        if (retryConfig == null) {
            // 自定义
            retryConfig = RetryConfig.custom()
                    // 最大尝试次数（默认为3次）
                    .maxAttempts(5)
                    // 重试时间间隔（默认为500 ms）
                    .waitDuration(Duration.ofSeconds(1))
                    .build();
        }
        return retryConfig;
    }

    @Bean(name = "retryRegistry")
    public RetryRegistry initRetryRegistry() {
        if (retryConfig == null) {
            this.initRetryConfig();
        }
        if (retryRegistry == null) {
            // 创建重试注册机
            retryRegistry = RetryRegistry.of(retryConfig);
            // 创建命名为exp的重试器
            retryRegistry.retry("exp");
        }
        return retryRegistry;
    }

    //## 6.6 时间限制器（TimeLimiter）
    @Bean(name = "timeLimiter")
    public TimeLimiterConfig initTimeLimiterConfig() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom() // 配置调用超时时间，默认值为1秒 .timeoutDuration(Duration.ofSeconds(2)) // 设置线程是否可中断将来再运行，默认值为true .cancelRunningFuture(false)
                .build();
        return timeLimiterConfig;
    }


    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
