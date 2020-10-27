package spring.cloud.product.hystrix.cmd;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixRequestCache;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.UserInfo;

public class UserGetCommand extends HystrixCommand<UserInfo> {

    private Long id = null; // 参数
    private RestTemplate restTemplate = null; // REST模板
    // 请求URL
    private final String URL = "http://USER/user/info/{id}";
    // Hystrix命令key
    private static final HystrixCommandKey COMMAND_KEY
            = HystrixCommandKey.Factory.asKey("user_get");

    /**
     * 构造方法
     *
     * @param restTemplate -- REST风格模板
     * @param id           -- 参数
     */
    public UserGetCommand(RestTemplate restTemplate, Long id) {
        // 在当前的命令中加入命令Key
        super(Setter.withGroupKey(
                HystrixCommandGroupKey.Factory.asKey("userGroup"))
                .andCommandKey(COMMAND_KEY));
        this.restTemplate = restTemplate;
        this.id = id;
    }

    @Override
    protected UserInfo run() throws Exception {
        System.out.println("获取用户" + id);
        UserInfo forObject = restTemplate.getForObject(URL, UserInfo.class, id);
        System.out.println(forObject);
        return forObject;
    }

    // 提供缓存键，以驱动Hystrix使用缓存
    @Override
    protected String getCacheKey() {
        return "user_" + id;
    }

    /**
     * 清除缓存
     *
     * @param id --用户编号
     */
    public static void clearCache(Long id) {
        String cacheKey = "user_" + id;
        // 根据命令key，清除缓存
        HystrixRequestCache.getInstance(COMMAND_KEY, HystrixConcurrencyStrategyDefault.getInstance()).clear(cacheKey);
    }


    /**
     * 降级方法
     *
     * @return 降级结果
     */
    protected UserInfo getFallback() {

        System.out.println("获取用户失败降级");
        return null;
    }
}
