package spring.cloud.product.facade.impl;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.product.facade.UserFacade;
import spring.cloud.product.hystrix.cmd.UserExpCommand;
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
        String url = "http://USER/hystrix/timeout";
        return restTemplate.getForObject(url, ResultMessage.class);
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallback2")
    public ResultMessage exp(String msg) {

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
}
