package spring.cloud.product.controller;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.common.vo.UserInfo;
import spring.cloud.product.facade.UserFacade;

@RestController
public class CircuitBreakerController {

    @Autowired
    private UserFacade userFacade;

    @GetMapping("/cr/timeout")
    public ResultMessage timeout() {

        return userFacade.timeout();
    }

    @GetMapping("/cr/timeout2")
    public ResultMessage timeout2() {

        return userFacade.timeout2();
    }

    @GetMapping("/cr/exp/{msg}")
    public ResultMessage exp(@PathVariable("msg") String msg) {
        return userFacade.exp(msg);
    }


    @GetMapping("/user/info/{id}")
    public UserInfo getUserInfo(@PathVariable("id") Long id) {
        return userFacade.testUserInfo(id);
    }


    @GetMapping("/user/info/cache/{id}")
    public UserInfo getUserInfo2(@PathVariable("id") Long id) {
        // 初始化HystrixRequestContext上下文
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            userFacade.getUserInfo(id);
            userFacade.getUserInfo(id);
            UserInfo user = new UserInfo(id, "user_name_update", "note_update");
            userFacade.updateUserInfo(user);
            return userFacade.getUserInfo(id);
        } finally {
            context.shutdown(); // 关闭上下文
        }
    }

}
