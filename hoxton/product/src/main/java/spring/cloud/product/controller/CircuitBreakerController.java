package spring.cloud.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.product.facade.UserFacade;

@RestController
public class CircuitBreakerController {

    @Autowired
    private UserFacade userFacade;

    @GetMapping("/cr/timeout")
    public ResultMessage timeout() {
        
        return userFacade.timeout();
    }

    @GetMapping("/cr/exp/{msg}")
    public ResultMessage exp(@PathVariable("msg") String msg) {
        return userFacade.exp(msg);
    }
}
