package spring.cloud.product.facade.impl;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.product.facade.UserFacade;

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
        String url = "http://USER/hystrix/timeout";
        return restTemplate.getForObject(url, ResultMessage.class);
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallback2")
    public ResultMessage exp(String msg) {
        com.netflix.hystrix.HystrixCommand
        String url = "http://USER/hystrix/exp/{msg}";
        return restTemplate.getForObject(url, ResultMessage.class, msg);
    }

    // 降级方法1
    public ResultMessage fallback1() {
        return new ResultMessage(false, "超时了");
    }

    /**
     * 降级方法2，带有参数
     *
     * @Param msg --消息
     * @Return ResultMessage --结果消息
     **/

    public ResultMessage fallback2(String msg) {
        return new ResultMessage(false, "调用产生异常了，参数:" + msg);
    }
}
