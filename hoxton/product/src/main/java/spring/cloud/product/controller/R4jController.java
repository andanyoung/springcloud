package spring.cloud.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.common.vo.UserInfo;
import spring.cloud.product.facade.R4jFacade;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/r4j")
@RestController
public class R4jController {
    @Autowired // 注入接口
    private R4jFacade r4jFacade = null;

    @GetMapping("/exp/{msg}")
    public ResultMessage exp(@PathVariable("msg") String msg) {
        return r4jFacade.exp(msg); // 业务逻辑调用
    }

    @GetMapping("/users")
    public List<UserInfo> getUserInfo() {
        // 开始时间
        Long start = System.currentTimeMillis();
        List<UserInfo> userList = new ArrayList<>();
        Long id = 0L;
        while (true) {
            id++;
            UserInfo user = r4jFacade.getUser(id); // 调用限速器
            userList.add(user);
            // 循环内的当前时间
            Long end = System.currentTimeMillis();
            // 超过1秒，终止循环
            if (end - start >= 1000) {
                break;
            }
        }
        return userList;
    }

    @GetMapping("/exp")
    public ResultMessage exp() {
        return r4jFacade.exp();
    }


}
