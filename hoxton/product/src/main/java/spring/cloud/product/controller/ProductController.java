package spring.cloud.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.ResultMessage;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    // 依赖注入RestTempalte
    @Autowired
    private RestTemplate restTemplate = null;

    @GetMapping("/purchase/{userId}/{productId}/{amount}")
    public ResultMessage purchaseProduct(
            @PathVariable("userId")  Long userId,
            @PathVariable("productId") Long productId,
            @PathVariable("amount") Double amount) {
        System.out.println("扣减产品余额。");
        // 这里的FUND代表资金微服务， RestTemplate会自动负载均衡
        String url = "http://FUND/fund/account/balance/{userId}/{amount}";// 封装请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("amount", amount); // 请求资金微服务
        ResultMessage rm = restTemplate.postForObject(url, null, ResultMessage.class, params ); // 打印资金微服务返回的消息
        System.out.println(rm.getMessage());
        System.out.println("记录交易信息");
        return new ResultMessage(true,"交易成功");
    }

}
