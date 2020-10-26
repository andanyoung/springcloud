package spring.cloud.product.hystrix.cmd;

import com.netflix.hystrix.HystrixCommand;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.ResultMessage;

public class UserTimeoutCommand extends HystrixCommand<ResultMessage> {


    // REST风格模板
    private RestTemplate restTemplate = null;

    /**
     * 构造方法，一般可以传递参数
     *
     * @param setter       -- 设置
     * @param restTemplate -- REST风格模板
     */
    public UserTimeoutCommand(Setter setter, RestTemplate restTemplate) {
        // 调用父类构造方法
        super(setter);
        this.restTemplate = restTemplate;
    }


    /**
     * 核心方法，命令执行逻辑
     */
    @Override
    protected ResultMessage run() throws Exception {
        String url = "http://USER/hystrix/timeout";
        return restTemplate.getForObject(url, ResultMessage.class);
    }

    /**
     * 降级方法
     *
     * @return 降级结果
     */
    @Override
    protected ResultMessage getFallback() {
        return new ResultMessage(false, "超时了");
    }
}
