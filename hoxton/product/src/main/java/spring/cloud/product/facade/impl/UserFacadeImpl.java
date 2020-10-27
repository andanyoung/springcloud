package spring.cloud.product.facade.impl;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.common.vo.UserInfo;
import spring.cloud.product.facade.UserFacade;
import spring.cloud.product.hystrix.cmd.UserExpCommand;
import spring.cloud.product.hystrix.cmd.UserGetCommand;
import spring.cloud.product.hystrix.cmd.UserPutCommand;
import spring.cloud.product.hystrix.cmd.UserTimeoutCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.channels.FileLockInterruptionException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class UserFacadeImpl implements UserFacade {

    // 注入RestTemplate，在Ribbon中我们标注了@LoadBalance，用以实现负载均衡
    @Autowired
    private RestTemplate restTemplate;


    @Override
    // @HystrixCommand将方法推给Hystrix进行监控
    // 配置项fallbackMethod指定了降级服务的方法
    @HystrixCommand(fallbackMethod = "fallback1")
    public ResultMessage timeout() {
        String url = "http://user/hystrix/timeout";
        ResultMessage forObject = restTemplate.getForObject(url, ResultMessage.class);
        return forObject;
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallback2",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000")
            })
    public ResultMessage exp(String msg) {

        String url = "http://user/hystrix/exp/{msg}";
        ResultMessage forObject = restTemplate.getForObject(url, ResultMessage.class, msg);
        return forObject;
    }

    // 降级方法1
    public ResultMessage fallback1(Throwable ex) {
        ex.printStackTrace();
        return new ResultMessage(false, "超时了");
    }

    /**
     * 降级方法2，带有参数
     *
     * @Param msg --消息
     * @Return ResultMessage --结果消息
     **/

    public ResultMessage fallback2(String msg, Throwable ex) {
        ex.printStackTrace();
        return new ResultMessage(false, "调用产生异常了，参数:" + msg);
    }

    /*---------------手动调用 HystrixCommand 方法。不使用Spring AOP----------------------------------------------------------------------------*/
    //    @HystrixCommand(fallbackMethod = "fallback1")
    @Override
    public ResultMessage timeout2() {
        // 命令分组（设置组名为“userGroup”）
        HystrixCommandGroupKey groupKey
                = HystrixCommandGroupKey.Factory.asKey("userGroup");
        // 创建Setter类
        com.netflix.hystrix.HystrixCommand.Setter setter
                = com.netflix.hystrix.HystrixCommand.Setter.withGroupKey(groupKey);
        // 创建命令
        UserTimeoutCommand userCmd = new UserTimeoutCommand(setter, restTemplate);
        // 同步执行命令
        return userCmd.execute();
        /***异步执行***
         * Future<ResultMessage> future = userCmd.queue();
         * try
         * {
         * // 发射参数，获取结果
         * return future.get();
         * } catch (Exception ex){
         * return userCmd.getFallback();
         *
         * }
         */
    }

    @HystrixCommand(fallbackMethod = "fallback1")
    public Future<ResultMessage> asyncTimeout() {
        return new AsyncResult<ResultMessage>() {
            @Override
            public ResultMessage invoke() {
                String url = "http://USER/hystrix/timeout";
                return restTemplate.getForObject(url, ResultMessage.class);
            }
        };
    }


    @Override
    public List<ResultMessage> exp2(String[] params) {
        // 命令分组（设置组名为“userGroup”）
        HystrixCommandGroupKey groupKey
                = HystrixCommandGroupKey.Factory.asKey("userGroup");
        // 创建Setter类
        com.netflix.hystrix.HystrixObservableCommand.Setter setter
                = com.netflix.hystrix.HystrixObservableCommand.Setter.withGroupKey(groupKey);
        // 创建命令
        UserExpCommand userCmd = new UserExpCommand(setter, restTemplate, params);
        List<ResultMessage> resList = new ArrayList<>();
        // 使用热观察者模式，它会立即执行描述的行为，从用户微服务得到数据
        Observable<ResultMessage> observable = userCmd.observe();
        // 使用冷观察者模式，它不会立即执行描述的行为，而是延迟
        // Observable<ResultMessage> observable = userCmd.toObservable();
        // 依次读出从观察者中得到的数据，Lambda表达式
        observable.forEach((ResultMessage resultMsg) -> { // ①
            resList.add(resultMsg);
        });
        // 同步执行命令
        return resList;
    }


    //用@HystrixCommand简化上述的代码
    @Override
    @HystrixCommand(fallbackMethod = "fallback3",
            // 执行模式
            observableExecutionMode = ObservableExecutionMode.EAGER)
    public Observable<ResultMessage> asyncExp(String[] params) {
        String url = "http://USER/hystrix/exp/{msg}";
        // 行为描述
        Observable.OnSubscribe<ResultMessage> onSubs = (resSubs) -> {
            try {
                int count = 0; // 计数器
                if (!resSubs.isUnsubscribed()) {
                    for (String param : params) {
                        count++;
                        System.out.println("第【" + count + "】次发送 ");
                        // 观察者发射单次参数到微服务
                        ResultMessage resMsg
                                = restTemplate.getForObject(
                                url, ResultMessage.class, param);
                        resSubs.onNext(resMsg);
                    }
                    // 遍历所有参数后，发射完结
                    resSubs.onCompleted();
                }
            } catch (Exception ex) {
                // 异常处理
                resSubs.onError(ex);
            }
        };
        return Observable.create(onSubs);
    }

    public ResultMessage fallback3(String[] params) {
        return new ResultMessage(false, "调用产生异常了，参数:" + params);
    }

    public ResultMessage fallback3(String msg, Throwable ex) {
        ex.printStackTrace();
        return new ResultMessage(false, "调用产生异常了，参数:" + msg);
    }


    @HystrixCommand(fallbackMethod = "fallback2",
            // 定义被忽略的异常，当发生这些异常时，不再执行降级方法
            ignoreExceptions = {FileNotFoundException.class, FileLockInterruptionException.class})
    public String dealFile(String filePath) {
        File file = new File(filePath);
        return file.getAbsolutePath();
    }


    public UserInfo testUserInfo(Long id) {

        // 初始化Hystrix命令请求上下文，如果没有，则抛出异常
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        UserGetCommand ugc1 = new UserGetCommand(restTemplate, id);
        UserGetCommand ugc2 = new UserGetCommand(restTemplate, id);
        UserGetCommand ugc3 = new UserGetCommand(restTemplate, id);
        UserPutCommand upc = new UserPutCommand(
                restTemplate, 1L, "user_name_update", "note_update");
        try {
            // 第一次请求添加缓存
            ugc1.execute();
            // 第二次请求从缓存中读取
            ugc2.execute();
            // 执行更新，删除缓存
            upc.execute();
            // 因为缓存已经被删除，所以重新请求
            return ugc3.execute();
        } finally {
            // 关闭上下文
            context.close();
        }
    }

    // 将结果缓存
    @CacheResult
    // 在默认情况下，命令键（commandKey）指向方法名getUserInfo
    @HystrixCommand
    @Override
    // @CacheKey 将参数id设置为缓存key
    public UserInfo getUserInfo(@CacheKey Long id) {
        String url = "http://USER/user/info/{id}";
        System.out.println("获取用户" + id);
        return restTemplate.getForObject(url, UserInfo.class, id);
    }

    // commandKey指定命令键，指向getUserInfo方法
    @CacheRemove(commandKey = "getUserInfo")
    @HystrixCommand
    @Override
    public UserInfo updateUserInfo(@CacheKey("id") UserInfo user) {
        String url = "http://USER/user/info";
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 封装请求实体对象，将用户信息对象设置为请求体
        HttpEntity<UserInfo> request = new HttpEntity<>(user, headers);
        System.out.println("执行更新用户" + user.getId());
        // 更新用户信息
        restTemplate.put(url, request);
        return user;
    }


    // 将结果缓存，cacheKeyMethod指定key的生成方法
    @CacheResult(cacheKeyMethod = "getCacheKey")
    // commandKey声明Hystrix命令键为“user_get”
    @HystrixCommand(commandKey = "user_get")
    @Override
    // 由于@CacheResult的配置项cacheKeyMethod高于@CacheKey，因此@CacheKey此处无效
    public UserInfo getUserInfo2(@CacheKey Long id) {
        String url = "http://USER/user/info/{id}";
        System.out.println("获取用户" + id);
        return restTemplate.getForObject(url, UserInfo.class, id);
    }

    // commandKey指定命令键，从而指向getUserInfo方法； cacheKeyMethod指定key的生成方法
    @CacheRemove(commandKey = "user_get", cacheKeyMethod = "getCacheKey")
    @HystrixCommand
    @Override
    public UserInfo updateUserInfo2(UserInfo user) {
        String url = "http://USER/user/info";
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 封装请求实体对象，将用户信息对象设置为请求体
        HttpEntity<UserInfo> request = new HttpEntity<>(user, headers);
        System.out.println("执行更新用户" + user.getId());
        // 更新用户信息
        restTemplate.put(url, request);
        return user;
    }

    private static final String CACHE_PREFIX = "user_"; // 前缀

    /**
     * 两个方法参数和命令方法保持一致
     **/
    public String getCacheKey(Long id) {
        return CACHE_PREFIX + id;
    }

    public String getCacheKey(UserInfo user) {
        return CACHE_PREFIX + user.getId();
    }

}
