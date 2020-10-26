package spring.cloud.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.common.vo.ResultMessage;

@RestController
@RequestMapping("/hystrix")
public class HystrixController {
    //最大休眠时间
    private static Long MAX_SLEEP_TIME = 5000L;

    /**
     * 随机超时测试，触发服务消费者启用断路器
     */
    @GetMapping("/timeout")
    public ResultMessage timeout() {
        // 产生一个小于5000的长整型随机数
        Long sleepTime = (long) (MAX_SLEEP_TIME * Math.random());
        try {
            // 线程按一个随机数字休眠，使得服务消费者能够存在一定的概率产生熔断
            Thread.sleep(sleepTime);
        } catch (Exception ex) {
            System.out.println("执行异常");
        }
        return new ResultMessage(true, "执行时间" + sleepTime);
    }

    /**
     * 异常测试，触发服务消费者启用断路
     */
    @GetMapping("/exp/{msg}")
    public ResultMessage exp(@PathVariable("msg") String msg) {
        if ("spring".equals(msg)) {
            return new ResultMessage(true, msg);
        } else {
            // 触发异常，让服务消费者启用熔断
            throw new RuntimeException(
                    "出现了异常，请检查参数msg是否为spring");
        }
    }
}
