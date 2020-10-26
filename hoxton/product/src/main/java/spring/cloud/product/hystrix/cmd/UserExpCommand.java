package spring.cloud.product.hystrix.cmd;

import com.netflix.hystrix.HystrixObservableCommand;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import rx.Subscriber;
import spring.cloud.common.vo.ResultMessage;

public class UserExpCommand extends HystrixObservableCommand<ResultMessage> {

    private RestTemplate restTemplate = null; // REST模板
    private String[] params = null; // 参数
    // 请求URL
    private final String URL = "http://USER/hystrix/exp/{msg}";

    /**
     * 构造方法
     *
     * @param setter       -- 设置
     * @param restTemplate -- REST风格模板
     * @param params       -- 参数
     */
    public UserExpCommand(Setter setter,
                          RestTemplate restTemplate, String[] params) {
        super(setter);
        this.restTemplate = restTemplate;
        this.params = params;
    }

    /**
     * 核心方法，向对应的微服务发射参数，
     */
    @Override
    protected Observable<ResultMessage> construct() {

        Observable.OnSubscribe<ResultMessage> subs // 定义行为
                = (Subscriber<? super ResultMessage> resSubs) -> {
            try {
                int count = 0; // 计数器
                if (!resSubs.isUnsubscribed()) {
                    for (String param : params) {
                        count++;
                        System.out.println("第【" + count + "】次发送 ");
                        // 观察者发射单次参数到微服务
                        ResultMessage resMsg
                                = restTemplate.getForObject(
                                URL, ResultMessage.class, param);
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
        return Observable.unsafeCreate(subs);
    }

    /**
     * 降级方法
     *
     * @return 降级结果
     */
    @Override
    protected Observable<ResultMessage> resumeWithFallback() {
        return Observable.error(new RuntimeException("发生异常了."));
    }
}
