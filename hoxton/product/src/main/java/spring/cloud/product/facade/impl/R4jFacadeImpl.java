package spring.cloud.product.facade.impl;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.common.vo.UserInfo;
import spring.cloud.product.facade.R4jFacade;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Service
public class R4jFacadeImpl implements R4jFacade {

    @Autowired // 注册机
    private CircuitBreakerRegistry circuitBreakerRegistry = null;

    @Autowired // 默认配置
            CircuitBreakerConfig circuitBreakerConfig = null;

    @Autowired // REST模板
    private RestTemplate restTemplate = null;

    // 错误结果ID
    private static final long ERROR_ID = Long.MAX_VALUE;

    @Override
    public ResultMessage exp(String msg) {
        // 根据配置创建并注册断路器（CircuitBreaker），键为“exp”，
        // 如果没有指定配置，则采用默认配置
        CircuitBreaker circuitBreaker
                = circuitBreakerRegistry.circuitBreaker("exp"); // ①
        // 描述事件，准备发送
        CheckedFunction0<ResultMessage> decoratedSupplier =
                CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
                        () -> {
                            String url = "http://USER/hystrix/exp/{msg}";
                            System.out.println("发送消息【" + msg + "】");
                            return restTemplate.getForObject(url,
                                    ResultMessage.class, msg);
                        }); // ②
        // 获取断路器的状态
        CircuitBreaker.State state = circuitBreaker.getState(); // ③
        System.out.println("断路器状态：【" + state.name() + "】");
        // 发送事件
        Try<ResultMessage> result = Try.of(decoratedSupplier)
                // 如果发生异常，则执行降级方法
                .recover(ex -> {
                    return new ResultMessage(false, ex.getMessage());
                }); // ④
        return result.get(); // ⑤
    }


    // 限速器注册机
    @Autowired
    private RateLimiterRegistry rateLimiterRegistry = null;

    @Override
    public UserInfo getUser(Long id) {
        // 获取或创建限速器
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("user");// ①
        // 定义事件，但是没有发送请求
        CheckedFunction0<UserInfo> decoratedSupplier
                = RateLimiter.decorateCheckedSupplier(rateLimiter,
                () -> reqUser(id)); // ②
        // 发送请求
        Try<UserInfo> result = Try.of(decoratedSupplier) // ③
                // 降级逻辑
                .recover(ex -> {
                    ex.printStackTrace();
                    return new UserInfo(ERROR_ID, "", ex.getMessage());
                });
        return result.get(); // ④
    }

    // 获取用户信息
    private UserInfo reqUser(Long id) {
        String url = "http://USER/user/info/{id}";
        System.out.println("获取用户" + id);
        return restTemplate.getForObject(url, UserInfo.class, id);
    }

    @Autowired
    BulkheadRegistry bulkheadRegistry;

    @Override
    public UserInfo getUser3(Long id) {
        // 获取舱壁
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("user"); // ①
        // 描述事件
        CheckedFunction0<UserInfo> decoratedSupplier  // ②
                = Bulkhead.decorateCheckedSupplier(
                bulkhead, () -> reqUser(id));
        // 发送请求
        Try<UserInfo> result = Try.of(decoratedSupplier) // ③
                .recover(ex -> { // 降级服务
                    ex.printStackTrace();
                    return new UserInfo(ERROR_ID, "", ex.getMessage());
                });
        return result.get(); // ④
    }


    // 注册重试注册机
    @Autowired
    private RetryRegistry retryRegistry = null;

    @Override
    public ResultMessage exp() {
        // 获取或创建重试
        Retry retry = retryRegistry.retry("timeout");
        // 监听重试事件
        retry.getEventPublisher()
                .onRetry(evt -> System.out.println("重试")); // ①
        // 描述事件
        CheckedFunction0<ResultMessage> decoratedSupplier
                = Retry.decorateCheckedSupplier(retry,
                () -> reqExp());
        // 发送请求
        Try<ResultMessage> result = Try.of(decoratedSupplier)
                // 降级逻辑
                .recover(ex -> new ResultMessage(false, "异常信息" + ex.getMessage()));
        return result.get();
    }

    private ResultMessage reqExp() {
        // 不使用注入的RestTemplate，原因是Ribbon存在重试机制
        RestTemplate restTmpl = new RestTemplate(); // ②
        String url = "http://localhost:6001/hystrix/exp/boot";
        return restTmpl.getForObject(url, ResultMessage.class);
    }


    /**
     * 获取Ecache的一个缓存实例
     *
     * @param id -- 用户编号
     **/
    private javax.cache.Cache<String, UserInfo> getCacheInstance(Long id) {
        // 获取缓存提供者，适合只有一个JCache实现的情况
        CachingProvider cachingProvider = Caching.getCachingProvider(); // ①
   /* // 如果系统有多种Jcache，则根据具体实现类名获取缓存提供者
   CachingProvider cachingProvider  // ②
      = Caching.getCachingProvider(
         "org.ehcache.jsr107.EhcacheCachingProvider");
   */
        // 获取缓存管理器
        CacheManager cacheManager = cachingProvider.getCacheManager();
        // 尝试获取名称为"user_"+id的缓存
        javax.cache.Cache<String, UserInfo> cacheInstance
                = cacheManager.getCache("user_" + id);
        if (cacheInstance == null) { // 获取失败，则创建缓存实例
            // 缓存配置类
            MutableConfiguration<String, UserInfo> config
                    = new MutableConfiguration<>(); // ③
            // 设置缓存键值类型
            config.setTypes(String.class, UserInfo.class);
            // 创建一个JCache对象，键值为"user_"+id
            cacheInstance = cacheManager.createCache("user_" + id, config);// ④
        }
        return cacheInstance;
    }


    @Override
    public UserInfo cacheUserInfo(Long id) {
        // 获取名称为【"user_"+id】的缓存
        javax.cache.Cache<String, UserInfo> cacheInstance = getCacheInstance(id);
        // 通过Resilience4j的Cache捆绑JCache的缓存实例
        // 此处的Cache类全限定名为io.github.resilience4j.cache.Cache
        // 和getCacheInstance方法的javax.cache.Cache不同
        Cache<String, UserInfo> cache = Cache.of(cacheInstance); // ①
        // 描述事件
        CheckedFunction1<String, UserInfo> cachedFunction = Cache.decorateCheckedSupplier(cache, () -> reqUser(id)); // ②
        // 获取结果，先从缓存获取，键为【"user_"+id】，失败则从执行请求逻辑
        UserInfo user = Try.of(() -> cachedFunction.apply("user_" + id)).get();
        return user;
    }

    @Autowired // 时间限制配置
    private TimeLimiterConfig timeLimiterConfig = null;

    @Override
    public ResultMessage timeout() {
        // 创建事件限制器
        TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfig); // ①
        // 采用单线程
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // 创建Supplier对象，并描述Supplier事件逻辑
        Supplier<Future<ResultMessage>> futureSupplier = // ②
                // submit方法，提交任务执行并等待返回结果
                () -> executorService.submit(() -> {
                    // 不使用注入的RestTemplate，因为不想启用Ribbon的超时机制
                    RestTemplate restTmpl = new RestTemplate();
                    String url = "http://localhost:6001/hystrix/timeout";
                    return restTmpl.getForObject(url, ResultMessage.class);
                });
        // 时间限制器捆绑事件
        Callable<ResultMessage> callable // ③
                = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
        // 获取结果
        Try<ResultMessage> result = Try.of(() -> callable.call())
                // 降级逻辑
                .recover(ex -> new ResultMessage(false, "执行超时"));
        return result.get();
    }

    // 混合使用组件
    @Override
    public UserInfo mixUserInfo(Long id) {
        // 断路器
        CircuitBreaker circuitBreaker
                = circuitBreakerRegistry.circuitBreaker("user");
        // 具体事件
        Callable<UserInfo> call = () -> reqUser(id);
        // 断路器绑定事件
        Callable<UserInfo> call1 = CircuitBreaker.decorateCallable(circuitBreaker, call);
        // 舱壁
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("user");
        // 舱壁捆绑断路器逻辑
        Callable<UserInfo> call2 = Bulkhead.decorateCallable(bulkhead, call1);
        // 获取或创建限速器
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("user");
        // 限速器捆绑舱壁事件
        Callable<UserInfo> call3 = RateLimiter.decorateCallable(rateLimiter, call2);
        // 重试机制
        Retry retry = retryRegistry.retry("timeout");
        // 重试捆绑事件
        Callable<UserInfo> call4 = Retry.decorateCallable(retry, call3);
        // 获取名称为"user_"+id的缓存实例
        javax.cache.Cache<String, UserInfo> cacheInstance = getCacheInstance(id);
        Cache<String, UserInfo> cache = Cache.of(cacheInstance);
        // 缓存捆绑限速事件
        CheckedFunction1<String, UserInfo> cacheFunc = Cache.decorateCallable(cache, call4);
        // 创建事件限制器
        TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfig);
        // 采用单线程池
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // 描述限时事件
        Supplier<Future<UserInfo>> supplier
                = () -> executorService.submit(() -> {
            UserInfo cacheResult = null;
            try {
                cacheResult = cacheFunc.apply("user_" + id);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return cacheResult;
        });
        // 限时器捆绑缓存事件
        Callable<UserInfo> call5 = TimeLimiter.decorateFutureSupplier(timeLimiter, supplier);
        // 获取结果
        Try<UserInfo> result = Try.of(() -> call5.call())
                // 降级逻辑
                .recover(ex -> new UserInfo(ERROR_ID, "", ex.getMessage()));
        return result.get();
    }
}
