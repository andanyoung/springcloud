package spring.cloud.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
// 在新版本的 Spring Cloud中 ，不再需要这个注解驱动服务发现了
//@EnableDiscoveryClient
public class ProductApplication {

    // 负载均衡
    @LoadBalanced
    // 创建Spring Bean
    @Bean
    public RestTemplate initRestTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class,args);
    }
}
