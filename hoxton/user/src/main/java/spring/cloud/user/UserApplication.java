package spring.cloud.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
// 在新版本的 Spring Cloud中 ，不再需要这个注解驱动服务发现了
@EnableDiscoveryClient
public class UserApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(UserApplication.class, args);
    }
}
