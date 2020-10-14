package spring.cloud.fund.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.common.vo.ResultMessage;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/fund")
public class AccountController {
    // 扣减账户资金
    @PostMapping("/account/balance/{userId}/{amount}")
    public ResultMessage deductingBalance(
            @PathVariable("userId") Long userId,
            @PathVariable("amount") Double amount,
            HttpServletRequest request) {
        // 打印当前服务的端口用于监测
        String message = "端口：【" + request.getServerPort() + "】扣减成功";
        return new ResultMessage(true, message);
    }
}
