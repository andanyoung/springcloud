package spring.cloud.fund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 在新版本的 Spring Cloud中 ，不再需要这个注解驱动服务发现了
//@EnableDiscoveryClient
public class FundApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundApplication.class,args);
    }
}
